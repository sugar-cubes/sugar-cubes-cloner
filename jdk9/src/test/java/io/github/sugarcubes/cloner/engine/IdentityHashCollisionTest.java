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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.sugarcubes.cloner.Cloner;
import io.github.sugarcubes.cloner.Cloners;
import io.github.sugarcubes.cloner.CloningMode;

/**
 * Slot maps and cloning when identity hash codes collide. The {@code hashCollisionTest} task runs it with
 * {@code -XX:hashCode=2}, which gives every object the same identity hash code; the regular test run checks it with
 * ordinary hash codes.
 */
class IdentityHashCollisionTest {

    static final class Node {

        final List<Node> children = new ArrayList<>();

        int value;

    }

    @Test
    void hashCodesCollideUnderTheTask() {
        if (Boolean.getBoolean("cloner.test.hashCollisions")) {
            Assertions.assertEquals(System.identityHashCode(new Object()), System.identityHashCode(new Object()));
        }
    }

    @Test
    void slotMapsFindEveryKey() {
        for (SlotMap map : new SlotMap[] {new IdentitySlotMap(), new ConcurrentSlotMap()}) {
            List<Object> keys = new ArrayList<>();
            List<CloneSlot> slots = new ArrayList<>();
            for (int i = 0; i < 2_000; i++) {
                Object key = new Object();
                keys.add(key);
                slots.add(map.getOrCreate(key, SingleThreadCloneSlot::new));
            }
            for (int i = 0; i < keys.size(); i++) {
                Assertions.assertSame(slots.get(i), map.get(keys.get(i)));
            }
            Assertions.assertNull(map.get(new Object()));
        }
    }

    @Test
    void graphIsCloned() {
        Node root = new Node();
        for (int i = 0; i < 300; i++) {
            Node child = new Node();
            child.value = i;
            child.children.add(root);
            root.children.add(child);
        }
        for (CloningMode mode : CloningMode.values()) {
            Cloner cloner = Cloners.builder().mode(mode).build();

            Node clone = cloner.clone(root);

            Assertions.assertEquals(300, clone.children.size());
            for (int i = 0; i < 300; i++) {
                Assertions.assertEquals(i, clone.children.get(i).value);
                Assertions.assertSame(clone, clone.children.get(i).children.get(0));
            }
        }
    }

}
