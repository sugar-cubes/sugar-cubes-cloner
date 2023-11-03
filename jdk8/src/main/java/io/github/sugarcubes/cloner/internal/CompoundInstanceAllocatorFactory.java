/*
 * Copyright 2017-2023 the original author or authors.
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

import io.github.sugarcubes.cloner.Checks;
import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.ConcurrentLazyCache;

public class CompoundInstanceAllocatorFactory implements InstanceAllocatorFactory {

    private final List<InstanceAllocatorFactory> allocators;
    private final ConcurrentLazyCache<Class<?>, InstanceAllocator<?>> cache = new ConcurrentLazyCache<>(this::newAllocatorUntyped);

    public CompoundInstanceAllocatorFactory(List<InstanceAllocatorFactory> allocators) {
        Checks.illegalArg(allocators.isEmpty(), "Must be at least one allocator.");
        this.allocators = new ArrayList<>(allocators);
    }

    public <T> InstanceAllocator<T> newAllocator(Class<T> type) {
        return (InstanceAllocator<T>) cache.get(type);
    }

    private InstanceAllocator<?> newAllocatorUntyped(Class<?> type) {
        return newAllocatorTyped(type);
    }

    private <T> InstanceAllocator<T> newAllocatorTyped(Class<T> type) {
        return new InstanceAllocator<T>() {

            private volatile InstanceAllocator<T> working;

            @Override
            public T newInstance() throws Exception {
                if (working != null) {
                    return working.newInstance();
                }

                synchronized (this) {

                    if (working != null) {
                        return working.newInstance();
                    }

                    List<Exception> errors = new ArrayList<>();
                    for (InstanceAllocatorFactory allocator : allocators) {
                        InstanceAllocator<T> ia = allocator.newAllocator(type);
                        try {
                            T instance = ia.newInstance();
                            working = ia;
                            return instance;
                        }
                        catch (Exception e) {
                            errors.add(e);
                        }
                    }
                    ClonerException error = new ClonerException(String.format("Cannot instantiate %s.", type.getName()));
                    errors.forEach(error::addSuppressed);
                    throw error;
                }

            }
        };
    }

}
