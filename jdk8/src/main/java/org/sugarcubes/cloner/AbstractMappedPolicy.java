package org.sugarcubes.cloner;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Copy policy implementation based on {@link Map}.
 *
 * @param <I> input object type
 * @param <K> map key type
 *
 * @author Maxim Butov
 */
public abstract class AbstractMappedPolicy<I, K> implements CopyPolicy<I> {

    /**
     * Map (key, copy action).
     */
    private final Map<K, CopyAction> map;

    /**
     * Creates policy.
     *
     * @param map map (key, action)
     */
    public AbstractMappedPolicy(Map<K, CopyAction> map) {
        this(map, LinkedHashMap::new);
    }

    /**
     * Creates policy.
     *
     * @param map map (key, action)
     * @param mapCopyConstructor copy constructor of map as method reference
     */
    public AbstractMappedPolicy(Map<K, CopyAction> map, Function<Map<K, CopyAction>, Map<K, CopyAction>> mapCopyConstructor) {
        Map<K, CopyAction> copy = mapCopyConstructor.apply(map);
        copy.entrySet().removeIf(e -> e.getValue() == CopyAction.DEFAULT);
        this.map = copy.isEmpty() ? Collections.emptyMap() : copy;
    }

    /**
     * Returns copy action by key or default if not found.
     *
     * @param key key
     * @return copy action
     */
    protected CopyAction get(K key) {
        return map.getOrDefault(key, CopyAction.DEFAULT);
    }

    /**
     * Returns map's entry set.
     *
     * @return map's entry set
     */
    protected Set<Map.Entry<K, CopyAction>> entrySet() {
        return map.entrySet();
    }

}
