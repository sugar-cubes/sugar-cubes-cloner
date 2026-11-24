/*
 * Copyright 2017-2026 the original author or authors.
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
package io.github.sugarcubes.cloner.platform;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.IntFunction;

import io.github.sugarcubes.cloner.spi.ContainerModel;

/**
 * Container models of well-known JDK collections and maps. Only these exact classes are supported: a subclass may
 * have its own state and is copied by fields. Maps and sets which hash their keys are supported only for lookups and
 * rebuilding, see {@link ContainerModel#hashesKeys()}.
 *
 * @author Maxim Butov
 */
public final class ContainerModels {

    private static final float LOAD_FACTOR = 0.75f;

    private final Map<Class<?>, ContainerModel<?>> models = new HashMap<>();

    /**
     * Creates the models.
     */
    public ContainerModels() {
        collection(ArrayList.class, ArrayList::new, false);
        collection(LinkedList.class, size -> new LinkedList<>(), false);
        collection(ArrayDeque.class, ArrayDeque::new, false);
        collection(Vector.class, Vector::new, false);
        collection(Stack.class, size -> new Stack<>(), false);
        collection(HashSet.class, size -> new HashSet<>(hashCapacity(size)), true);
        collection(LinkedHashSet.class, size -> new LinkedHashSet<>(hashCapacity(size)), true);
        map(HashMap.class, original -> new HashMap<>(hashCapacity(original.size())), true);
        map(LinkedHashMap.class, ContainerModels::emptyLinkedHashMap, true);
        map(Hashtable.class, original -> new Hashtable<>(hashCapacity(original.size())), true);
        map(ConcurrentHashMap.class, original -> new ConcurrentHashMap<>(hashCapacity(original.size())), true);
        map(IdentityHashMap.class, original -> new IdentityHashMap<>(original.size()), false);
        map(EnumMap.class, ContainerModels::emptyEnumMap, false);
    }

    /**
     * Returns the container model of the type.
     *
     * @param <T> type
     * @param type type
     * @return model or empty if the type is not a supported container
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<ContainerModel<T>> containerModel(Class<T> type) {
        return Optional.ofNullable((ContainerModel<T>) models.get(type));
    }

    private <T extends Collection<Object>> void collection(Class<?> type, IntFunction<T> constructor, boolean hashes) {
        models.put(type, new CollectionModel<>(constructor, hashes));
    }

    private <T extends Map<Object, Object>> void map(Class<?> type, Function<T, T> constructor, boolean hashes) {
        models.put(type, new MapModel<>(constructor, hashes));
    }

    private static int hashCapacity(int size) {
        return (int) (size / LOAD_FACTOR) + 1;
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> emptyLinkedHashMap(Map<Object, Object> original) {
        // the clone keeps the access order and the load factor, which are not public
        LinkedHashMap<Object, Object> clone = (LinkedHashMap<Object, Object>) ((LinkedHashMap<Object, Object>) original).clone();
        clone.clear();
        return clone;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<Object, Object> emptyEnumMap(Map<Object, Object> original) {
        // the clone keeps the key type
        EnumMap clone = ((EnumMap) original).clone();
        clone.clear();
        return clone;
    }

    /**
     * Model of a collection.
     */
    private static final class CollectionModel<T extends Collection<Object>> implements ContainerModel<T> {

        private final IntFunction<T> constructor;

        private final boolean hashes;

        CollectionModel(IntFunction<T> constructor, boolean hashes) {
            this.constructor = constructor;
            this.hashes = hashes;
        }

        @Override
        public boolean isMap() {
            return false;
        }

        @Override
        public boolean hashesKeys() {
            return hashes;
        }

        @Override
        public T create(T original) {
            return constructor.apply(original.size());
        }

        @Override
        public Object[] elements(T container) {
            return container.toArray();
        }

        @Override
        public void fill(T container, Object[] elements) {
            container.addAll(Arrays.asList(elements));
        }

        @Override
        public boolean containsKey(T container, Object key) {
            return container.contains(key);
        }

        @Override
        public void clear(T container) {
            container.clear();
        }

    }

    /**
     * Model of a map.
     */
    private static class MapModel<T extends Map<Object, Object>> implements ContainerModel<T> {

        private final Function<T, T> constructor;

        private final boolean hashes;

        MapModel(Function<T, T> constructor, boolean hashes) {
            this.constructor = constructor;
            this.hashes = hashes;
        }

        @Override
        public boolean isMap() {
            return true;
        }

        @Override
        public boolean hashesKeys() {
            return hashes;
        }

        @Override
        public T create(T original) {
            return constructor.apply(original);
        }

        @Override
        public Object[] elements(T container) {
            Object[] elements = new Object[container.size() * 2];
            int index = 0;
            for (Map.Entry<Object, Object> entry : container.entrySet()) {
                elements[index++] = entry.getKey();
                elements[index++] = entry.getValue();
            }
            return elements;
        }

        @Override
        public void fill(T container, Object[] elements) {
            for (int k = 0; k < elements.length; k += 2) {
                container.put(elements[k], elements[k + 1]);
            }
        }

        @Override
        public boolean containsKey(T container, Object key) {
            return container.containsKey(key);
        }

        @Override
        public void clear(T container) {
            container.clear();
        }

    }

}
