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
package io.github.sugarcubes.cloner.platform;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.spi.ValuesModel;

/**
 * {@link TypeModels} of JDK 9+: immutable collections of {@link List#of}, {@link Set#of} and {@link Map#of} rebuilt
 * from their elements.
 *
 * @author Maxim Butov
 */
class TypeModels9 extends TypeModels8 {

    /**
     * Models of immutable collections by their classes (the classes are not public).
     */
    private final Map<Class<?>, ValuesModel<?>> immutableCollections = immutableCollectionModels();

    private Map<Class<?>, ValuesModel<?>> immutableCollectionModels() {
        Map<Class<?>, ValuesModel<?>> models = new HashMap<>();
        // samples give the classes of the implementations
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();
        for (List<?> list : Arrays.asList(List.of(), List.of(a), List.of(a, b, c))) {
            models.put(list.getClass(), ImmutableCollectionModel.list(list.getClass(), this::immutableList));
        }
        for (Set<?> set : Arrays.asList(Set.of(), Set.of(a), Set.of(a, b, c))) {
            models.put(set.getClass(), ImmutableCollectionModel.set(set.getClass()));
        }
        for (Map<?, ?> map : Arrays.asList(Map.of(), Map.of(a, a), Map.of(a, a, b, b))) {
            models.put(map.getClass(), ImmutableCollectionModel.map(map.getClass()));
        }
        return Collections.unmodifiableMap(models);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<ValuesModel<T>> valuesModel(Class<T> type, Reflection reflection) {
        ValuesModel<T> model = (ValuesModel<T>) immutableCollections.get(type);
        return model != null ? Optional.of(model) : super.valuesModel(type, reflection);
    }

    /**
     * Creates an immutable list of the elements.
     *
     * @param values elements
     * @return immutable list
     */
    List<Object> immutableList(Object[] values) {
        return List.of(values);
    }

}
