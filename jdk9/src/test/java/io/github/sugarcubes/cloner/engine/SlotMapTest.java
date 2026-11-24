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
package io.github.sugarcubes.cloner.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SlotMapTest {

    static SlotMap newMap(String type) {
        return "identity".equals(type) ? new IdentitySlotMap() : new ConcurrentSlotMap();
    }

    @ParameterizedTest
    @ValueSource(strings = {"identity", "concurrent"})
    void keysAreComparedByIdentity(String type) {
        SlotMap map = newMap(type);
        String a = new String("a");
        String b = new String("a");
        CloneSlot slot = new SingleThreadCloneSlot(a, a);

        map.put(slot);

        Assertions.assertSame(slot, map.get(a));
        Assertions.assertNull(map.get(b));
    }

    @ParameterizedTest
    @ValueSource(strings = {"identity", "concurrent"})
    void getOrCreateCreatesOnce(String type) {
        SlotMap map = newMap(type);
        Object key = new Object();
        int[] created = new int[1];
        Function<Object, CloneSlot> factory = original -> {
            created[0]++;
            return new SingleThreadCloneSlot(original);
        };

        CloneSlot first = map.getOrCreate(key, factory);
        CloneSlot second = map.getOrCreate(key, factory);

        Assertions.assertSame(first, second);
        Assertions.assertEquals(1, created[0]);
    }

    @ParameterizedTest
    @ValueSource(strings = {"identity", "concurrent"})
    void manyKeysSurviveResizing(String type) {
        SlotMap map = newMap(type);
        List<Object> keys = new ArrayList<>();
        List<CloneSlot> slots = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            Object key = new Object();
            keys.add(key);
            slots.add(map.getOrCreate(key, SingleThreadCloneSlot::new));
        }

        for (int i = 0; i < keys.size(); i++) {
            Assertions.assertSame(slots.get(i), map.get(keys.get(i)));
        }
        Assertions.assertNull(map.get(new Object()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"identity", "concurrent"})
    void putReplaces(String type) {
        SlotMap map = newMap(type);
        Object key = new Object();
        CloneSlot replacement = new SingleThreadCloneSlot(key, key);

        map.getOrCreate(key, SingleThreadCloneSlot::new);
        map.put(replacement);

        Assertions.assertSame(replacement, map.get(key));
    }

    @Test
    void concurrentGetOrCreateCreatesOneSlotPerKey() throws Exception {
        SlotMap map = new ConcurrentSlotMap();
        List<Object> keys = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            keys.add(new Object());
        }
        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<List<CloneSlot>>> results = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                results.add(executor.submit(() -> {
                    List<CloneSlot> slots = new ArrayList<>();
                    keys.forEach(key -> slots.add(map.getOrCreate(key, ConcurrentCloneSlot::new)));
                    return slots;
                }));
            }
            List<CloneSlot> first = results.get(0).get();
            for (Future<List<CloneSlot>> result : results) {
                List<CloneSlot> slots = result.get();
                for (int i = 0; i < keys.size(); i++) {
                    Assertions.assertSame(first.get(i), slots.get(i));
                    Assertions.assertSame(first.get(i), map.get(keys.get(i)));
                }
            }
        }
        finally {
            executor.shutdown();
        }
    }

    @Test
    void readersSeeSlotsInsertedByOtherThreads() throws Exception {
        SlotMap map = new ConcurrentSlotMap();
        // a queue publishes inserted keys to the readers
        Queue<Object> inserted = new ConcurrentLinkedQueue<>();
        int writers = 4;
        int readers = 4;
        int keysPerWriter = 50_000;
        AtomicInteger checked = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(writers + readers);
        try {
            List<Future<?>> results = new ArrayList<>();
            for (int w = 0; w < writers; w++) {
                results.add(executor.submit(() -> {
                    for (int i = 0; i < keysPerWriter; i++) {
                        Object key = new Object();
                        map.getOrCreate(key, ConcurrentCloneSlot::new);
                        inserted.add(key);
                    }
                }));
            }
            for (int r = 0; r < readers; r++) {
                results.add(executor.submit(() -> {
                    while (checked.get() < writers * keysPerWriter) {
                        Object key = inserted.poll();
                        if (key != null) {
                            Assertions.assertNotNull(map.get(key));
                            checked.incrementAndGet();
                        }
                    }
                }));
            }
            for (Future<?> result : results) {
                result.get();
            }
        }
        finally {
            executor.shutdown();
        }
    }

    @Test
    void concurrentInsertionsDuringResizes() throws Exception {
        SlotMap map = new ConcurrentSlotMap();
        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<List<Object>>> results = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                results.add(executor.submit(() -> {
                    List<Object> keys = new ArrayList<>();
                    for (int i = 0; i < 200_000; i++) {
                        Object key = new Object();
                        CloneSlot slot = map.getOrCreate(key, ConcurrentCloneSlot::new);
                        Assertions.assertSame(key, slot.original);
                        Assertions.assertSame(slot, map.get(key));
                        keys.add(key);
                    }
                    return keys;
                }));
            }
            for (Future<List<Object>> result : results) {
                for (Object key : result.get()) {
                    Assertions.assertSame(key, map.get(key).original);
                }
            }
        }
        finally {
            executor.shutdown();
        }
    }

}
