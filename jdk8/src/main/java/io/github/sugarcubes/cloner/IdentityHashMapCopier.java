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

import java.util.IdentityHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copier for {@link IdentityHashMap}. {@link ReflectionCopier} is not suitable for {@link IdentityHashMap} because the result's
 * {@link IdentityHashMap#table} has its entries on wrong places because clones have different identity hash codes than original
 * objects. Thus, the map has to be entirely rebuilt.
 *
 * @author Maxim Butov
 */
public class IdentityHashMapCopier extends TwoPhaseObjectCopier<IdentityHashMap<Object, Object>> {

    @Override
    public IdentityHashMap<Object, Object> allocate(IdentityHashMap<Object, Object> original) throws Exception {
        return new IdentityHashMap<>(original.size());
    }

    @Override
    public void deepCopy(IdentityHashMap<Object, Object> original, IdentityHashMap<Object, Object> clone, CopyContext context)
        throws Exception {
        Object[] keys = original.keySet().toArray();
        Object[] values = original.values().toArray();
        // entries are put when all keys and values are known, some may be copied later (reference cycles)
        AtomicInteger unknown = new AtomicInteger(keys.length + values.length + 1);
        for (int k = 0; k < keys.length; k++) {
            int index = k;
            context.copy(keys[k], key -> {
                keys[index] = key;
                putAllIfKnown(clone, keys, values, unknown);
            });
            context.copy(values[k], value -> {
                values[index] = value;
                putAllIfKnown(clone, keys, values, unknown);
            });
        }
        putAllIfKnown(clone, keys, values, unknown);
    }

    private static void putAllIfKnown(IdentityHashMap<Object, Object> clone, Object[] keys, Object[] values,
        AtomicInteger unknown) {
        if (unknown.decrementAndGet() == 0) {
            for (int k = 0; k < keys.length; k++) {
                clone.put(keys[k], values[k]);
            }
        }
    }

}
