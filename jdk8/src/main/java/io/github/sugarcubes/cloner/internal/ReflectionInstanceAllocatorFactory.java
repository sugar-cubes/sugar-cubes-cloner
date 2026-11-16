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

import java.util.Optional;

import io.github.sugarcubes.cloner.ClassUtils;

/**
 * Instance allocator factory which calls the constructor without parameters via reflection.
 * Supports only types which declare such constructor.
 *
 * @author Maxim Butov
 */
public class ReflectionInstanceAllocatorFactory implements InstanceAllocatorFactory {

    private final Reflection reflection;

    /**
     * Constructor.
     *
     * @param reflection reflection to access constructors
     */
    public ReflectionInstanceAllocatorFactory(Reflection reflection) {
        this.reflection = reflection;
    }

    @Override
    public <T> Optional<InstanceAllocator<T>> newAllocator(Class<T> type) {
        if (!ClassUtils.isAllocatable(type) || !ClassUtils.hasNoArgConstructor(type)) {
            return Optional.empty();
        }
        return Optional.of(reflection.makeAccessible(reflection.getDeclaredConstructor(type))::newInstance);
    }

    @Override
    public boolean invokesConstructor() {
        return true;
    }

}
