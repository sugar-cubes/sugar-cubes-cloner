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
 * Low-level model of a container: a collection or a map. Knows how to create an empty container of the same kind, to
 * list its elements, to fill a container with elements and to look up a key; what and how to copy is decided by the
 * caller.
 *
 * @param <T> container type
 * @author Maxim Butov
 */
public interface ContainerModel<T> {

    /**
     * Tells whether the container is a map: its elements are keys and values in pairs.
     *
     * @return {@code true} for a map
     */
    boolean isMap();

    /**
     * Tells whether the container uses hash codes of its keys (set elements or map keys). Hash codes of copied keys
     * are final only when the whole object graph is copied.
     *
     * @return {@code true} if keys are hashed
     */
    boolean hashesKeys();

    /**
     * Creates an empty container of the same kind as the original: the same class and configuration, e.g. the key
     * type of an {@link java.util.EnumMap}.
     *
     * @param original original container
     * @return empty container
     */
    T create(T original);

    /**
     * Lists the elements of the container in iteration order; for a map keys and values in pairs.
     *
     * @param container container
     * @return elements
     */
    Object[] elements(T container);

    /**
     * Fills the empty container with the elements, in the order of {@link #elements(Object)}.
     *
     * @param container empty container
     * @param elements elements
     */
    void fill(T container, Object[] elements);

    /**
     * Tells whether the container finds the key (a set element or a map key).
     *
     * @param container container
     * @param key key
     * @return {@code true} if the key is found
     */
    boolean containsKey(T container, Object key);

    /**
     * Removes all the elements, keeping the configuration of the container.
     *
     * @param container container
     */
    void clear(T container);

}
