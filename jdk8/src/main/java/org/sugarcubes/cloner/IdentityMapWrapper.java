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
package org.sugarcubes.cloner;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Replacement for {@link java.util.IdentityHashMap} with "usual" map as delegate.
 *
 * @author Maxim Butov
 */
public class IdentityMapWrapper<K, V> extends AbstractMap<K, V> {

    /**
     * Delegate key.
     */
    static final class IdentityKeyWrapper<K> {

        final K key;
        final int hash;

        IdentityKeyWrapper(K key) {
            this.key = key;
            this.hash = System.identityHashCode(key);
        }

        @Override
        public boolean equals(Object obj) {
            return ((IdentityKeyWrapper<?>) obj).key == key;
        }

        @Override
        public int hashCode() {
            return hash;
        }

    }

    /**
     * Delegate map.
     */
    private final Map<IdentityKeyWrapper<K>, V> delegate;

    /**
     * Creates map and initializes {@link #delegate} from #supplier.
     *
     * @param supplier delegate supplier
     */
    public IdentityMapWrapper(Supplier<Map<?, ?>> supplier) {
        this(supplier, Collections.emptyMap());
    }

    /**
     * Creates map, initializes {@link #delegate} from #supplier, puts initial entries from #initial.
     *
     * @param supplier delegate supplier
     * @param initial initial entries
     */
    public IdentityMapWrapper(Supplier<Map<?, ?>> supplier, Map<K, V> initial) {
        delegate = (Map) supplier.get();
        initial.forEach((key, value) -> delegate.put(new IdentityKeyWrapper<>(key), value));
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public V get(Object key) {
        return delegate.get(new IdentityKeyWrapper<>(key));
    }

    @Override
    public V put(K key, V value) {
        return delegate.put(new IdentityKeyWrapper<>(key), value);
    }

    @Override
    public V remove(Object key) {
        return delegate.remove(new IdentityKeyWrapper<>(key));
    }

    class EntryIterator implements Iterator<Entry<K, V>> {

        final Iterator<Entry<IdentityKeyWrapper<K>, V>> iterator = delegate.entrySet().iterator();

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            Entry<IdentityKeyWrapper<K>, V> entry = iterator.next();
            return new Entry<K, V>() {
                @Override
                public K getKey() {
                    return entry.getKey().key;
                }

                @Override
                public V getValue() {
                    return entry.getValue();
                }

                @Override
                public V setValue(V value) {
                    return entry.setValue(value);
                }
            };
        }

    }

    class EntrySet extends AbstractSet<Entry<K, V>> {

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public void clear() {
            delegate.clear();
        }

    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

}
