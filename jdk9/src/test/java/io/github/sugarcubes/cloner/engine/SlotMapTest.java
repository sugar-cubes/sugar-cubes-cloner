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
package io.github.sugarcubes.cloner.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
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
        CloneSlot slot = new SingleThreadCloneSlot(a);

        map.put(a, slot);

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
            return new SingleThreadCloneSlot(key);
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
            slots.add(map.getOrCreate(key, original -> new SingleThreadCloneSlot(key)));
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
        CloneSlot replacement = new SingleThreadCloneSlot(key);

        map.getOrCreate(key, original -> new SingleThreadCloneSlot(key));
        map.put(key, replacement);

        Assertions.assertSame(replacement, map.get(key));
    }

}
