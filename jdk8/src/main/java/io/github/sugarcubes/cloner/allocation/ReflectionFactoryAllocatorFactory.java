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
package io.github.sugarcubes.cloner.allocation;

import java.lang.reflect.Constructor;
import java.util.Optional;

import io.github.sugarcubes.cloner.spi.InstanceAllocator;
import io.github.sugarcubes.cloner.spi.InstanceAllocatorFactory;
import io.github.sugarcubes.cloner.util.ClassUtils;
import io.github.sugarcubes.cloner.util.ClonerExceptionUtils;
import sun.reflect.ReflectionFactory;

/**
 * Creates instances with {@code sun.reflect.ReflectionFactory} of the {@code jdk.unsupported} module, the mechanism of
 * deserialization: the constructor of {@link Object} only is called. Needs neither {@code Unsafe}, nor opened
 * packages, nor additional libraries; a critical internal API (JEP 260), not planned for removal.
 *
 * @author Maxim Butov
 */
public final class ReflectionFactoryAllocatorFactory implements InstanceAllocatorFactory {

    private static final String REFLECTION_FACTORY = "sun.reflect.ReflectionFactory";

    private final ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();

    private final Constructor<Object> objectConstructor =
        ClonerExceptionUtils.replaceException(() -> Object.class.getDeclaredConstructor());

    /**
     * Tells whether the mechanism is available: a runtime image may lack the {@code jdk.unsupported} module.
     *
     * @return {@code true} if the mechanism can be used
     */
    public static boolean isAvailable() {
        return ClassUtils.isClassAvailable(REFLECTION_FACTORY);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<InstanceAllocator<T>> newAllocator(Class<T> type) {
        if (!ClassUtils.isAllocatable(type)) {
            return Optional.empty();
        }
        // created once per type, already accessible
        Constructor<T> constructor = (Constructor<T>) reflectionFactory.newConstructorForSerialization(type, objectConstructor);
        // null instead of an empty array of arguments: a method reference would allocate one on every call
        return Optional.of(() -> constructor.newInstance((Object[]) null));
    }

}
