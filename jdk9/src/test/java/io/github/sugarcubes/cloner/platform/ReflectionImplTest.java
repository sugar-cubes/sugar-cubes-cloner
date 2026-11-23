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
package io.github.sugarcubes.cloner.platform;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.sugarcubes.cloner.spi.Reflection;

class ReflectionImplTest {

    static class RecordingOpener implements TypeOpener {

        final List<Class<?>> opened = new ArrayList<>();

        @Override
        public void open(Class<?> type) {
            opened.add(type);
        }

    }

    static class Sample {

        private int value;

    }

    private final RecordingOpener opener = new RecordingOpener();

    private final Reflection reflection = new ReflectionImpl9(opener);

    @Test
    void enumeratingFieldsDoesNotOpenType() {
        reflection.getDeclaredFields(Sample.class);
        reflection.getDeclaredField(Sample.class, "value");

        Assertions.assertEquals(List.of(), opener.opened);
    }

    @Test
    void makingAccessibleOpensDeclaringType() {
        Field field = reflection.makeAccessible(reflection.getDeclaredField(Sample.class, "value"));

        Assertions.assertTrue(field.canAccess(new Sample()));
        Assertions.assertEquals(List.of(Sample.class), opener.opened);
    }

}
