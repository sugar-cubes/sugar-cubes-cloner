/**
 * Copyright 2017-2022 the original author or authors.
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
package org.sugarcubes.cloner;

/**
 * Object allocator interface.
 *
 * @author Maxim Butov
 */
public interface ObjectAllocator {

    /**
     * Creates default allocator. It will be {@link ObjectAllocator} if the Objenesis library available or
     * {@link ReflectionAllocator} otherwise.
     *
     * @return default allocator
     */
    static ObjectAllocator defaultAllocator() {
        return ObjenesisUtils.isObjenesisAvailable() ? new ObjenesisAllocator() : new ReflectionAllocator();
    }

    /**
     * Creates an instance of the specified type. Instance may be not initialized.
     *
     * @param <T> object type
     * @param type type to instantiate
     * @return new instance of the type
     * @throws Exception if something went wrong
     */
    <T> T newInstance(Class<T> type) throws Exception;

    /**
     * Creates an object factory for the type.
     *
     * @param <T> object type
     * @param type type
     * @return object factory
     */
    default <T> ObjectFactory<T> getFactory(Class<T> type) {
        return () -> newInstance(type);
    }

}
