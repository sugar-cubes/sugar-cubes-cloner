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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.sugarcubes.cloner.allocation.MethodHandleInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.allocation.ObjenesisInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.allocation.ReflectionInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.platform.JdkPlatform;
import io.github.sugarcubes.cloner.spi.InstanceAllocator;
import io.github.sugarcubes.cloner.spi.InstanceAllocatorFactory;

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

    static class Counted {

        static final AtomicInteger CONSTRUCTED = new AtomicInteger();

        int value;

        Counted() {
            CONSTRUCTED.incrementAndGet();
        }

    }

    private static final InstanceAllocatorFactory NOTHING_SUPPORTED = new InstanceAllocatorFactory() {
        @Override
        public <T> Optional<InstanceAllocator<T>> newAllocator(Class<T> type) {
            return Optional.empty();
        }
    };

    private final ReflectionInstanceAllocatorFactory constructorAllocator =
        new ReflectionInstanceAllocatorFactory(JdkPlatform.current().reflection());

    @Test
    void allocatorDoesNotCallConstructor() {
        Cloner cloner = cloner(Toolsets.builder().allocator(new ObjenesisInstanceAllocatorFactory()));
        Counted original = new Counted();
        original.value = 7;
        int constructed = Counted.CONSTRUCTED.get();

        Counted clone = cloner.clone(original);

        Assertions.assertEquals(7, clone.value);
        Assertions.assertEquals(constructed, Counted.CONSTRUCTED.get());
    }

    @Test
    void constructorFallbackIsUsedForTypesNotSupportedByAllocators() {
        Cloner cloner = cloner(Toolsets.builder().allocator(NOTHING_SUPPORTED).constructorFallback(constructorAllocator));
        int constructed = Counted.CONSTRUCTED.get();

        cloner.clone(new Counted());

        Assertions.assertEquals(constructed + 2, Counted.CONSTRUCTED.get());
    }

    @Test
    void allocatorsMustNotCallConstructors() {
        ToolsetBuilder builder = Toolsets.builder();

        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.allocator(constructorAllocator));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> builder.allocators(new ObjenesisInstanceAllocatorFactory(), new MethodHandleInstanceAllocatorFactory()));
    }

    @Test
    void noAllocatorsAndNoFallbackIsMisconfiguration() {
        ToolsetBuilder builder = Toolsets.builder().allocators().noConstructorFallback();

        Assertions.assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void superclassWithoutDefaultConstructorDoesNotNeedAllocator() {
        Cloner cloner = cloner(Toolsets.builder().allocators().constructorFallback(constructorAllocator));

        Child clone = cloner.clone(new Child());

        Assertions.assertEquals(42, clone.value);
    }

    @Test
    void unsupportedTypeFailsOnCloning() {
        Cloner cloner = cloner(Toolsets.builder().allocators().constructorFallback(constructorAllocator));

        ClonerException error =
            Assertions.assertThrows(ClonerException.class, () -> cloner.clone(new NoDefaultConstructor(7)));

        Assertions.assertTrue(error.getMessage().contains(NoDefaultConstructor.class.getName()), error.getMessage());
    }

    private static Cloner cloner(ToolsetBuilder toolset) {
        return Cloners.builder().toolset(toolset.build()).build();
    }

}
