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

/**
 * Instance allocator factory interface.
 *
 * @author Maxim Butov
 */
@FunctionalInterface
public interface InstanceAllocatorFactory {

    /**
     * Prepares an allocator for the type. Whether the factory supports the type is decided here, so the allocator
     * fails only because of the environment or the object itself.
     *
     * @param <T> object type
     * @param type type
     * @return allocator, or empty if the factory cannot create instances of the type
     */
    <T> Optional<InstanceAllocator<T>> newAllocator(Class<T> type);

    /**
     * Tells whether allocators of this factory call a constructor of the type. Constructors may have side effects
     * which a clone should not repeat.
     *
     * @return {@code true} if allocators call constructors, {@code false} if they only allocate objects
     */
    default boolean invokesConstructor() {
        return false;
    }

}
