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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Entry whose key or value is copied later.
     */
    private static final class PendingEntry {

        private final Object originalKey;

        private final Object originalValue;

        private Object key;

        private Object value;

        PendingEntry(Object originalKey, Object originalValue, Object key, Object value) {
            this.originalKey = originalKey;
            this.originalValue = originalValue;
            this.key = key;
            this.value = value;
        }

        int unknownCount() {
            return (key == CopyContext.PENDING ? 1 : 0) + (value == CopyContext.PENDING ? 1 : 0);
        }

    }

    @Override
    public void deepCopy(IdentityHashMap<Object, Object> original, IdentityHashMap<Object, Object> clone, CopyContext context)
        throws Exception {
        List<PendingEntry> pending = null;
        for (Map.Entry<Object, Object> entry : original.entrySet()) {
            Object key = context.copyNow(entry.getKey());
            Object value = context.copyNow(entry.getValue());
            if (key != CopyContext.PENDING && value != CopyContext.PENDING) {
                clone.put(key, value);
                continue;
            }
            if (pending == null) {
                pending = new ArrayList<>();
            }
            pending.add(new PendingEntry(entry.getKey(), entry.getValue(), key, value));
        }
        if (pending != null) {
            putLater(clone, pending, context);
        }
    }

    /**
     * Puts pending entries when all of them are known, after the loop of {@link #deepCopy} has finished.
     */
    private static void putLater(IdentityHashMap<Object, Object> clone, List<PendingEntry> pending, CopyContext context)
        throws Exception {
        AtomicInteger unknown = new AtomicInteger(pending.stream().mapToInt(PendingEntry::unknownCount).sum() + 1);
        for (PendingEntry entry : pending) {
            if (entry.key == CopyContext.PENDING) {
                context.subscribe(entry.originalKey, later -> {
                    entry.key = later;
                    putAllIfKnown(clone, pending, unknown);
                });
            }
            if (entry.value == CopyContext.PENDING) {
                context.subscribe(entry.originalValue, later -> {
                    entry.value = later;
                    putAllIfKnown(clone, pending, unknown);
                });
            }
        }
        putAllIfKnown(clone, pending, unknown);
    }

    private static void putAllIfKnown(IdentityHashMap<Object, Object> clone, List<PendingEntry> pending,
        AtomicInteger unknown) {
        if (unknown.decrementAndGet() == 0) {
            pending.forEach(entry -> clone.put(entry.key, entry.value));
        }
    }

}
