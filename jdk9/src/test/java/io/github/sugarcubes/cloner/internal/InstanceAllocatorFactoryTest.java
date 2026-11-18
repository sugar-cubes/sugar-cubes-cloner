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
package io.github.sugarcubes.cloner.internal;

import java.util.AbstractList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.sugarcubes.cloner.spi.InstanceAllocatorFactory;
import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.spi.TypeOpener;

class InstanceAllocatorFactoryTest {

    static class NoDefaultConstructor {

        NoDefaultConstructor(int value) {
        }

    }

    static class PrivateDefaultConstructor {

        private PrivateDefaultConstructor() {
        }

    }

    private static final Class<?>[] NOT_INSTANTIABLE = {List.class, AbstractList.class, int[].class, int.class};

    private final Reflection reflection = JdkPlatform.current().reflection(TypeOpener.NOOP);

    @Test
    void allocatorsWithoutConstructorSupportAnyConcreteClass() throws Exception {
        InstanceAllocatorFactory[] factories = {
            new ObjenesisInstanceAllocatorFactory(),
            new UnsafeAllocatorFactory(UnsafeBridge.getInstance(reflection)),
        };
        for (InstanceAllocatorFactory factory : factories) {
            Assertions.assertInstanceOf(NoDefaultConstructor.class,
                factory.newAllocator(NoDefaultConstructor.class).get().newInstance(), factory.toString());
            assertNotInstantiable(factory);
        }
    }

    @Test
    void constructorAllocatorsSupportOnlyClassesWithNoArgConstructor() throws Exception {
        InstanceAllocatorFactory[] factories = {
            new ReflectionInstanceAllocatorFactory(reflection),
            new MethodHandleInstanceAllocatorFactory(),
        };
        for (InstanceAllocatorFactory factory : factories) {
            Assertions.assertInstanceOf(PrivateDefaultConstructor.class,
                factory.newAllocator(PrivateDefaultConstructor.class).get().newInstance(), factory.toString());
            Assertions.assertFalse(factory.newAllocator(NoDefaultConstructor.class).isPresent(), factory.toString());
            assertNotInstantiable(factory);
        }
    }

    private static void assertNotInstantiable(InstanceAllocatorFactory factory) {
        for (Class<?> type : NOT_INSTANTIABLE) {
            Assertions.assertFalse(factory.newAllocator(type).isPresent(), () -> factory + " " + type);
        }
    }

}
