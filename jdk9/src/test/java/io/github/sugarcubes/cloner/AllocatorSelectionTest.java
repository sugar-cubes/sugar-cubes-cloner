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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.sugarcubes.cloner.internal.ObjenesisInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.ReflectionImpl;
import io.github.sugarcubes.cloner.internal.ReflectionInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.TypeOpener;

class AllocatorSelectionTest {

    static class NoDefaultConstructor {

        final int value;

        NoDefaultConstructor(int value) {
            this.value = value;
        }

    }

    static class Child extends NoDefaultConstructor {

        Child() {
            super(42);
        }

    }

    private final ReflectionInstanceAllocatorFactory constructorAllocator =
        new ReflectionInstanceAllocatorFactory(new ReflectionImpl(TypeOpener.NOOP));

    @Test
    void compoundSkipsAllocatorWhichDoesNotSupportType() {
        Cloner cloner = cloner(new ToolsetBuilder().allocators(constructorAllocator, new ObjenesisInstanceAllocatorFactory()));

        NoDefaultConstructor clone = cloner.clone(new NoDefaultConstructor(7));

        Assertions.assertEquals(7, clone.value);
    }

    @Test
    void superclassWithoutDefaultConstructorDoesNotNeedAllocator() {
        Cloner cloner = cloner(new ToolsetBuilder().allocator(constructorAllocator));

        Child clone = cloner.clone(new Child());

        Assertions.assertEquals(42, clone.value);
    }

    @Test
    void unsupportedTypeFailsOnCloning() {
        Cloner cloner = cloner(new ToolsetBuilder().allocator(constructorAllocator));

        ClonerException error =
            Assertions.assertThrows(ClonerException.class, () -> cloner.clone(new NoDefaultConstructor(7)));

        Assertions.assertTrue(error.getMessage().contains(NoDefaultConstructor.class.getName()), error.getMessage());
    }

    private static Cloner cloner(ToolsetBuilder toolset) {
        return Cloners.builder().toolset(toolset.build()).build();
    }

}
