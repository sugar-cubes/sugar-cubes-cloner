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
package io.github.sugarcubes.cloner.generated;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.sugarcubes.cloner.Cloners;
import io.github.sugarcubes.cloner.CopyAction;
import io.github.sugarcubes.cloner.CopyContext;
import io.github.sugarcubes.cloner.ObjectCopier;
import io.github.sugarcubes.cloner.ReflectionClonerBuilder;

/**
 * Fields of immutable final types are copied as is only if the cloner copies their values as is: type and object
 * policies and custom copiers are respected, with generated copiers and without them.
 */
class SharedValuesTest {

    enum Planet {
        EARTH {
            @Override
            int moons() {
                return 1;
            }
        },
        MARS;

        int moons() {
            return 2;
        }
    }

    static class Holder {

        String text = new String("text");
        LocalDate date = LocalDate.of(2026, 11, 23);
        Planet planet = Planet.EARTH;
        Integer number = 1000;

    }

    private static ReflectionClonerBuilder builder(boolean generated) {
        return Cloners.builder().generatedCopiers(generated);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void immutableValuesAreShared(boolean generated) {
        Holder original = new Holder();

        Holder clone = builder(generated).build().clone(original);

        Assertions.assertSame(original.text, clone.text);
        Assertions.assertSame(original.date, clone.date);
        Assertions.assertSame(Planet.EARTH, clone.planet);
        Assertions.assertSame(original.number, clone.number);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void typePolicyIsRespected(boolean generated) {
        Holder clone = builder(generated).typeAction(String.class, CopyAction.NULL).build().clone(new Holder());

        Assertions.assertNull(clone.text);
        Assertions.assertNotNull(clone.date);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void objectPolicyIsRespected(boolean generated) {
        Holder original = new Holder();

        Holder clone = builder(generated).objectAction(original.text, CopyAction.NULL).build().clone(original);

        Assertions.assertNull(clone.text);
        Assertions.assertSame(original.date, clone.date);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void customCopierIsRespected(boolean generated) {
        ObjectCopier<LocalDate> nextDay = (LocalDate date, CopyContext context) -> context.register(date.plusDays(1));
        Holder original = new Holder();

        Holder clone = builder(generated).copier(LocalDate.class, nextDay).build().clone(original);

        Assertions.assertEquals(original.date.plusDays(1), clone.date);
    }

}
