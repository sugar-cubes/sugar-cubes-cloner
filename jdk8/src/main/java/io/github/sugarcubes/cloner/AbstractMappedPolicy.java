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
