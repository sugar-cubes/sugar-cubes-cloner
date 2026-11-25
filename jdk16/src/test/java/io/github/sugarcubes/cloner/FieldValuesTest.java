/*
 * Copyright 2017-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.sugarcubes.cloner;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * CopyAction.CLEAR and values of fields, for classes copied by fields (generated or not) and for records.
 */
class FieldValuesTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface Id {
    }

    static class Entity {

        @Id
        Long id = 10L;

        int version = 1;

        boolean active = true;

        double rate = 1.5;

        final List<String> lines = new ArrayList<>(Arrays.asList("a", "b"));

        String note = "note";

    }

    record Point(int x, String label, List<String> tags) {
    }

    record Checked(String name) {

        Checked {
            Objects.requireNonNull(name, "name");
        }

    }

    private static ReflectionClonerBuilder builder(boolean generated) {
        return Cloners.builder().generatedCopiers(generated);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void clearGivesDefaultValuesAlsoToPrimitives(boolean generated) {
        Entity clone = builder(generated)
            .fieldAction(Entity.class, "version", CopyAction.CLEAR)
            .fieldAction(Entity.class, "active", CopyAction.CLEAR)
            .fieldAction(Entity.class, "rate", CopyAction.CLEAR)
            .fieldAction(Entity.class, "note", CopyAction.CLEAR)
            .build().clone(new Entity());

        // field initializers do not run
        Assertions.assertEquals(0, clone.version);
        Assertions.assertFalse(clone.active);
        Assertions.assertEquals(0.0, clone.rate);
        Assertions.assertNull(clone.note);
        Assertions.assertEquals(Arrays.asList("a", "b"), clone.lines);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void valuesAreSuppliedForEveryClone(boolean generated) {
        Cloner cloner = builder(generated)
            .fieldValue(Entity.class, "lines", ArrayList::new)
            .fieldValue(Entity.class, "version", () -> 7)
            .fieldValue(Predicates.annotatedWith(Id.class), () -> null)
            .build();
        Entity original = new Entity();

        Entity first = cloner.clone(original);
        Entity second = cloner.clone(original);

        Assertions.assertNull(first.id);
        Assertions.assertEquals(7, first.version);
        Assertions.assertTrue(first.lines.isEmpty());
        Assertions.assertNotSame(first.lines, second.lines);
        Assertions.assertEquals("note", first.note);
        Assertions.assertTrue(first.active);
    }

    @Test
    void valueHasPriorityOverAction() {
        Entity clone = Cloners.builder()
            .fieldAction(Entity.class, "note", CopyAction.CLEAR)
            .fieldValue(Entity.class, "note", () -> "draft")
            .build().clone(new Entity());

        Assertions.assertEquals("draft", clone.note);
    }

    @Test
    void valueMustFitTheField() {
        Cloner nullPrimitive = Cloners.builder().fieldValue(Entity.class, "version", () -> null).build();
        Cloner wrongType = Cloners.builder().fieldValue(Entity.class, "note", () -> 1).build();

        ClonerException error = Assertions.assertThrows(ClonerException.class, () -> nullPrimitive.clone(new Entity()));
        Assertions.assertTrue(error.getMessage().contains("does not fit field"), error.getMessage());
        Assertions.assertThrows(ClonerException.class, () -> wrongType.clone(new Entity()));
    }

    @Test
    void staticFieldsTakeNoValues() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> Cloners.builder().fieldValue(FieldValuesTest.class.getDeclaredField("STATIC"), () -> 1));
    }

    @SuppressWarnings("checkstyle:VisibilityModifier")
    static int STATIC = 1;

    @Test
    void recordComponentsFollowFieldActionsAndValues() {
        List<String> tags = new ArrayList<>(Arrays.asList("t"));
        Point original = new Point(3, "label", tags);

        Point clone = Cloners.builder()
            .fieldAction(Point.class, "x", CopyAction.CLEAR)
            .fieldAction(Point.class, "tags", CopyAction.ORIGINAL)
            .fieldValue(Point.class, "label", () -> "copy")
            .build().clone(original);

        Assertions.assertEquals(new Point(0, "copy", tags), clone);
        Assertions.assertSame(tags, clone.tags());
    }

    @Test
    void recordConstructorChecksClearedComponents() {
        Cloner cloner = Cloners.builder().fieldAction(Checked.class, "name", CopyAction.CLEAR).build();

        ClonerException error = Assertions.assertThrows(ClonerException.class, () -> cloner.clone(new Checked("a")));

        Assertions.assertInstanceOf(NullPointerException.class, error.getCause());
    }

}
