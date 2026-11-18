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
package io.github.sugarcubes.cloner.spi;

/**
 * Low-level model of a type whose instances are created from the values of their components, e.g. a record or an
 * immutable collection. Knows how to read the values and to create an instance from them; what and how to copy is
 * decided by the caller.
 *
 * @param <T> type
 * @author Maxim Butov
 */
public interface ValuesModel<T> {

    /**
     * Returns the type.
     *
     * @return type
     */
    Class<T> getType();

    /**
     * Returns the name of the component for messages.
     *
     * @param index index of the value
     * @return name of the component
     */
    String nameOf(int index);

    /**
     * Tells whether the values are hashed by the created instance (elements of a set, keys of a map). Hash codes of
     * copied values may be final only when the whole object graph is copied.
     *
     * @return {@code true} if values (or keys, see {@link #isMap()}) are hashed
     */
    default boolean hashesKeys() {
        return false;
    }

    /**
     * Tells whether the values are keys and values of a map in pairs.
     *
     * @return {@code true} for a map
     */
    default boolean isMap() {
        return false;
    }

    /**
     * Reads the values of the components.
     *
     * @param object object
     * @return values of the components
     */
    Object[] read(T object);

    /**
     * Creates an instance from the values of the components. If the constructor fails, throws
     * {@link io.github.sugarcubes.cloner.ClonerException} with the exception of the constructor as the cause.
     *
     * @param values values of the components
     * @return new instance
     */
    T create(Object[] values);

}
