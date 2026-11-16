/*
 * Copyright 2017-2024 the original author or authors.
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Canonical constructor of a record gets completely copied components.
 */
class RecordComponentsTest {

    record DefensiveCopy(List<String> items) {

        DefensiveCopy {
            items = new ArrayList<>(items);
        }

    }

    record Validated(List<String> items) {

        Validated {
            if (items.isEmpty()) {
                throw new IllegalArgumentException("items must not be empty");
            }
        }

    }

    record Node(String name, List<Object> items) {
    }

    record CopyingNode(List<Object> items) {

        CopyingNode {
            items = new ArrayList<>(items);
        }

    }

    record ValidatedNode(List<Object> items) {

        ValidatedNode {
            if (items.isEmpty()) {
                throw new IllegalArgumentException("items must not be empty");
            }
        }

    }

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @ParameterizedTest
    @EnumSource(CloningMode.class)
    void constructorWithDefensiveCopy(CloningMode mode) {
        DefensiveCopy original = new DefensiveCopy(List.of("a", "b"));

        DefensiveCopy clone = cloner(mode).clone(original);

        Assertions.assertEquals(List.of("a", "b"), clone.items());
    }

    @ParameterizedTest
    @EnumSource(CloningMode.class)
    void constructorWithValidation(CloningMode mode) {
        Validated original = new Validated(new ArrayList<>(List.of("a")));

        Validated clone = cloner(mode).clone(original);

        Assertions.assertEquals(List.of("a"), clone.items());
    }

    @ParameterizedTest
    @EnumSource(CloningMode.class)
    void recordInReferenceCycle(CloningMode mode) {
        Node original = new Node("a", new ArrayList<>());
        original.items().add("first");
        original.items().add(original);

        Node clone = cloner(mode).clone(original);

        Assertions.assertNotSame(original, clone);
        Assertions.assertNotSame(original.items(), clone.items());
        Assertions.assertEquals(2, clone.items().size());
        Assertions.assertEquals("first", clone.items().get(0));
        Assertions.assertSame(clone, clone.items().get(1));
    }

    @ParameterizedTest
    @EnumSource(CloningMode.class)
    void recordInReferenceCycleWithDefensiveCopyIsReported(CloningMode mode) {
        List<Object> items = new ArrayList<>();
        CopyingNode original = new CopyingNode(items);
        // the record keeps its own copy, so the cycle goes through it
        original.items().add(original);

        ClonerException error = Assertions.assertThrows(ClonerException.class, () -> cloner(mode).clone(original));

        Assertions.assertTrue(error.getMessage().contains("replaces the component 'items'"), error.getMessage());
    }

    @ParameterizedTest
    @EnumSource(CloningMode.class)
    void recordInReferenceCycleWithValidationIsReported(CloningMode mode) {
        ValidatedNode original = new ValidatedNode(new ArrayList<>(List.of("first")));
        original.items().add(original);

        ClonerException error = Assertions.assertThrows(ClonerException.class, () -> cloner(mode).clone(original));

        Assertions.assertTrue(error.getMessage().contains("constructor fails"), error.getMessage());
        Assertions.assertInstanceOf(IllegalArgumentException.class, error.getCause());
    }

    @Test
    void sharedRecordInParallelBranches() {
        DefensiveCopy shared = new DefensiveCopy(List.of("a", "b"));
        List<List<Object>> original = branches(shared);

        List<List<Object>> clone = Assertions.assertTimeoutPreemptively(TIMEOUT,
            () -> cloner(CloningMode.PARALLEL).clone(original));

        Object sharedClone = clone.get(0).get(0);
        Assertions.assertEquals(List.of("a", "b"), ((DefensiveCopy) sharedClone).items());
        clone.forEach(branch -> Assertions.assertSame(sharedClone, branch.get(0)));
    }

    @Test
    void sharedRecordCycleInParallelBranches() {
        Node shared = new Node("a", new ArrayList<>());
        shared.items().add(shared);
        List<List<Object>> original = branches(shared);

        List<List<Object>> clone = Assertions.assertTimeoutPreemptively(TIMEOUT,
            () -> cloner(CloningMode.PARALLEL).clone(original));

        Node sharedClone = (Node) clone.get(0).get(0);
        Assertions.assertNotSame(shared, sharedClone);
        Assertions.assertSame(sharedClone, sharedClone.items().get(0));
        clone.forEach(branch -> Assertions.assertSame(sharedClone, branch.get(0)));
    }

    @Test
    void sharedRecordCycleWithDefensiveCopyInParallelBranchesDoesNotHang() {
        CopyingNode shared = new CopyingNode(new ArrayList<>());
        shared.items().add(shared);
        List<List<Object>> original = branches(shared);

        Assertions.assertTimeoutPreemptively(TIMEOUT,
            () -> Assertions.assertThrows(ClonerException.class, () -> cloner(CloningMode.PARALLEL).clone(original)));
    }

    private static List<List<Object>> branches(Object shared) {
        List<List<Object>> branches = new ArrayList<>();
        for (int k = 0; k < 200; k++) {
            branches.add(new ArrayList<>(Collections.singletonList(shared)));
        }
        return branches;
    }

    private static Cloner cloner(CloningMode mode) {
        return Cloners.builder().mode(mode).build();
    }

}
