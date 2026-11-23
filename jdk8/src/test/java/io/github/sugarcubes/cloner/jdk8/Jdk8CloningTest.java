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
package io.github.sugarcubes.cloner.jdk8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.sugarcubes.cloner.Cloners;
import io.github.sugarcubes.cloner.CloningMode;

/**
 * Cloning on JDK 8: the base version of the multi-release jar.
 */
class Jdk8CloningTest {

    static final class Key {

        final String name;

        Key(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Key && ((Key) obj).name.equals(name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

    }

    static final class Holder {

        List<Object> list = new ArrayList<>(Arrays.asList("a", 1, 2.5));
        LinkedList<String> linked = new LinkedList<>(Arrays.asList("b", "c"));
        Map<Key, String> map = new HashMap<>();
        TreeMap<String, Integer> sorted = new TreeMap<>();
        HashSet<Key> set = new HashSet<>();
        Object[] array = {"d", new Date(42)};
        TimeUnit unit = TimeUnit.SECONDS;
        Holder self = this;

        Holder() {
            map.put(new Key("k"), "v");
            sorted.put("x", 1);
            set.add(new Key("s"));
        }

    }

    @ParameterizedTest
    @EnumSource(CloningMode.class)
    void graphIsCloned(CloningMode mode) {
        Holder original = new Holder();

        Holder clone = Cloners.builder().mode(mode).build().clone(original);

        Assertions.assertNotSame(original, clone);
        Assertions.assertSame(clone, clone.self);
        Assertions.assertEquals(original.list, clone.list);
        Assertions.assertNotSame(original.list, clone.list);
        Assertions.assertEquals(original.linked, clone.linked);
        Assertions.assertEquals("v", clone.map.get(new Key("k")));
        Assertions.assertNotSame(original.map.keySet().iterator().next(), clone.map.keySet().iterator().next());
        Assertions.assertEquals(original.sorted, clone.sorted);
        Assertions.assertTrue(clone.set.contains(new Key("s")));
        Assertions.assertArrayEquals(original.array, clone.array);
        Assertions.assertNotSame(original.array[1], clone.array[1]);
        Assertions.assertSame(TimeUnit.SECONDS, clone.unit);
    }

}
