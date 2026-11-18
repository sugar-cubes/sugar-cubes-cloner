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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import io.github.sugarcubes.cloner.spi.ValuesModel;

/**
 * Values model of immutable collections created by {@link List#of}, {@link Set#of} and {@link Map#of}: the values are
 * the elements (keys and values in pairs for a map), instances are created by the same factory methods. Empty
 * collections keep their identity, the factories return singletons.
 *
 * @param <T> collection type
 * @author Maxim Butov
 */
final class ImmutableCollectionModel<T> implements ValuesModel<T> {

    private final Class<T> type;

    private final Function<T, Object[]> reader;

    private final Function<Object[], T> factory;

    private final boolean hashes;

    private final boolean map;

    private ImmutableCollectionModel(Class<T> type, Function<T, Object[]> reader, Function<Object[], T> factory,
        boolean hashes, boolean map) {
        this.type = type;
        this.reader = reader;
        this.factory = factory;
        this.hashes = hashes;
        this.map = map;
    }

    @SuppressWarnings("unchecked")
    static ValuesModel<?> list(Class<?> type, Function<Object[], List<Object>> factory) {
        return new ImmutableCollectionModel<>((Class<List<Object>>) type, Collection::toArray, factory, false, false);
    }

    @SuppressWarnings("unchecked")
    static ValuesModel<?> set(Class<?> type) {
        return new ImmutableCollectionModel<>((Class<Set<Object>>) type, Collection::toArray, Set::of, true, false);
    }

    @SuppressWarnings("unchecked")
    static ValuesModel<?> map(Class<?> type) {
        return new ImmutableCollectionModel<>((Class<Map<Object, Object>>) type, ImmutableCollectionModel::entries,
            ImmutableCollectionModel::ofEntries, true, true);
    }

    private static Object[] entries(Map<Object, Object> map) {
        Object[] values = new Object[map.size() * 2];
        int index = 0;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            values[index++] = entry.getKey();
            values[index++] = entry.getValue();
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> ofEntries(Object[] values) {
        Map.Entry<Object, Object>[] entries = new Map.Entry[values.length / 2];
        for (int k = 0; k < entries.length; k++) {
            entries[k] = Map.entry(values[2 * k], values[2 * k + 1]);
        }
        return Map.ofEntries(entries);
    }

    /**
     * Creates a list with {@link List#of}, which does not accept nulls.
     *
     * @param values elements
     * @return list
     */
    static List<Object> listOf(Object[] values) {
        return List.of(values);
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public String nameOf(int index) {
        return map ? (index % 2 == 0 ? "key " : "value ") + index / 2 : "element " + index;
    }

    @Override
    public boolean hashesKeys() {
        return hashes;
    }

    @Override
    public boolean isMap() {
        return map;
    }

    @Override
    public Object[] read(T object) {
        return reader.apply(object);
    }

    @Override
    public T create(Object[] values) {
        return factory.apply(Arrays.copyOf(values, values.length));
    }

}
