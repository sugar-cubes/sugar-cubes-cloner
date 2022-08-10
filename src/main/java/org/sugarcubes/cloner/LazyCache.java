package org.sugarcubes.cloner;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Lazy cache with mapping function.
 *
 * @author Maxim Butov
 */
public final class LazyCache<K, V> {

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
    public LazyCache(Function<K, V> mappingFunction) {
        this(Collections.emptyMap(), mappingFunction);
    }

    /**
     * Constructor.
     *
     * @param initialMap initial cache entries
     * @param mappingFunction key-value mapping function
     */
    public LazyCache(Map<K, V> initialMap, Function<K, V> mappingFunction) {
        this.mappingFunction = mappingFunction;
        this.cache = new ConcurrentHashMap<>(initialMap);
    }

    /**
     * Returns value by key, creates if necessary.
     *
     * @param key key
     * @return value
     */
    public V get(K key) {
        // get is faster than computeIfAbsent, try it first
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
