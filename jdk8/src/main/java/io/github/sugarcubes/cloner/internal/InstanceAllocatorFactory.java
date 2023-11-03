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

import io.github.sugarcubes.cloner.ObjenesisUtils;
import io.github.sugarcubes.cloner.ReflectionInstanceAllocatorFactory;

/**
 * Instance allocator factory interface.
 *
 * @author Maxim Butov
 */
public interface InstanceAllocatorFactory extends TypeAllocator {

    /**
     * Creates default instance allocator factory. It will be {@link InstanceAllocatorFactory} if the Objenesis library available or
     * {@link ReflectionInstanceAllocatorFactory} otherwise.
     *
     * @return default allocator
     */
    static InstanceAllocatorFactory defaultInstance() {
        return ObjenesisUtils.isObjenesisAvailable() ? new ObjenesisInstanceAllocatorFactory() : new ReflectionInstanceAllocatorFactory();
    }

    /**
     * Creates an object factory for the type.
     *
     * @param <T> object type
     * @param type type
     * @return object factory
     */
    default <T> InstanceAllocator<T> newAllocator(Class<T> type) {
        return () -> allocateInstance(type);
    }

    @Override
    default <T> T allocateInstance(Class<T> type) throws Exception {
        return newAllocator(type).newInstance();
    }
    
}
