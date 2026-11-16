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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copier for {@link EnumMap}.
 *
 * @author Maxim Butov
 */
public class EnumMapCopier<K extends Enum<K>, V> extends TwoPhaseObjectCopier<EnumMap<K, V>> {

    @Override
    public EnumMap<K, V> allocate(EnumMap<K, V> original) throws Exception {
        // keeps the key type, values are put by the second phase
        EnumMap<K, V> clone = original.clone();
        clone.clear();
        return clone;
    }

    @Override
    public void deepCopy(EnumMap<K, V> original, EnumMap<K, V> clone, CopyContext context) throws Exception {
        List<K> keys = new ArrayList<>(original.keySet());
        Object[] values = original.values().toArray();
        // values are put when all of them are known, some may be copied later (reference cycles)
        AtomicInteger unknown = new AtomicInteger(values.length + 1);
        for (int k = 0; k < values.length; k++) {
            int index = k;
            context.copy(values[k], value -> {
                values[index] = value;
                putAllIfKnown(clone, keys, values, unknown);
            });
        }
        putAllIfKnown(clone, keys, values, unknown);
    }

    @SuppressWarnings("unchecked")
    private static <K extends Enum<K>, V> void putAllIfKnown(EnumMap<K, V> clone, List<K> keys, Object[] values,
        AtomicInteger unknown) {
        if (unknown.decrementAndGet() == 0) {
            for (int k = 0; k < values.length; k++) {
                clone.put(keys.get(k), (V) values[k]);
            }
        }
    }

}
