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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github.sugarcubes.cloner.Checks;
import io.github.sugarcubes.cloner.ConcurrentLazyCache;

/**
 * Instance allocator factory which uses for every type the first factory supporting it.
 *
 * @author Maxim Butov
 */
public class CompoundInstanceAllocatorFactory implements InstanceAllocatorFactory {

    private final List<InstanceAllocatorFactory> factories;

    private final ConcurrentLazyCache<Class<?>, Optional<?>> cache = new ConcurrentLazyCache<>(this::findAllocator);

    /**
     * Constructor.
     *
     * @param factories instance allocator factories in order of preference
     */
    public CompoundInstanceAllocatorFactory(List<InstanceAllocatorFactory> factories) {
        Checks.illegalArg(factories.isEmpty(), "Must be at least one allocator.");
        this.factories = new ArrayList<>(factories);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<InstanceAllocator<T>> newAllocator(Class<T> type) {
        return (Optional<InstanceAllocator<T>>) cache.get(type);
    }

    @Override
    public boolean invokesConstructor() {
        return factories.stream().anyMatch(InstanceAllocatorFactory::invokesConstructor);
    }

    private Optional<?> findAllocator(Class<?> type) {
        return findTypedAllocator(type);
    }

    private <T> Optional<InstanceAllocator<T>> findTypedAllocator(Class<T> type) {
        for (InstanceAllocatorFactory factory : factories) {
            Optional<InstanceAllocator<T>> allocator = factory.newAllocator(type);
            if (allocator.isPresent()) {
                return allocator;
            }
        }
        return Optional.empty();
    }

}
