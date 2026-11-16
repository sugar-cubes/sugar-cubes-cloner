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
package io.github.sugarcubes.cloner;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Lazy cache with mapping function.
 *
 * @author Maxim Butov
 */
public class ConcurrentLazyCache<K, V> {

    /**
     * Key-value mapping function.
     */
    private final Function<K, V> mappingFunction;

    /**
     * The cache implementation.
     */
    private final Map<K, V> cache;

    /**
     * Constructor.
     *
     * @param mappingFunction key-value mapping function
     */
    public ConcurrentLazyCache(Function<K, V> mappingFunction) {
        this(Collections.emptyMap(), mappingFunction);
    }

    /**
     * Constructor.
     *
     * @param initialMap initial cache entries
     * @param mappingFunction key-value mapping function
     */
    public ConcurrentLazyCache(Map<K, V> initialMap, Function<K, V> mappingFunction) {
        this.mappingFunction = mappingFunction;
        this.cache = new ConcurrentHashMap<>(initialMap);
    }

    /**
     * Returns value by key, does not create if absent.
     *
     * @param key key
     * @return value or {@code null}
     */
    public V getIfPresent(K key) {
        return cache.get(key);
    }

    /**
     * Returns value by key, creates if necessary.
     *
     * @param key key
     * @return value
     */
    public V get(K key) {
        // get() is faster than computeIfAbsent(), try it first
        V value = cache.get(key);
        return value != null ? value : cache.computeIfAbsent(key, mappingFunction);
    }

    /**
     * Puts (key, value) into cache.
     *
     * @param key key
     * @param value value
     * @return previous value
     */
    public V put(K key, V value) {
        return cache.put(key, value);
    }

    /**
     * Puts map into cache.
     *
     * @param map map
     */
    public void putAll(Map<? extends K, ? extends V> map) {
        cache.putAll(map);
    }

}
