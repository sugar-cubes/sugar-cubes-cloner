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
package io.github.sugarcubes.cloner.check;

import java.time.DayOfWeek;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import io.github.sugarcubes.cloner.Cloners;

/**
 * Clones a graph of common JDK types and own classes without any {@code --add-opens} option, run by the
 * {@code noOpensCheck} task on JDK 17. Exits with a non-zero code if cloning fails or the clone differs.
 */
public final class NoOpensCheck {

    record Point(int x, int y) {
    }

    static final class Key {
    }

    static final class Graph {

        final List<String> list = new ArrayList<>(List.of("a", "b"));
        final LinkedList<Integer> linked = new LinkedList<>(List.of(1, 2));
        final ArrayDeque<String> deque = new ArrayDeque<>(List.of("c"));
        final Map<String, Integer> map = new HashMap<>(Map.of("d", 3));
        final Set<String> set = new HashSet<>(Set.of("e"));
        final List<String> immutableList = List.of("f", "g");
        final Set<Key> immutableSet;
        final Map<Key, String> immutableMap;
        final Key key = new Key();
        final Map<Object, Object> identity = new IdentityHashMap<>(Map.of("h", "i"));
        final EnumMap<DayOfWeek, String> days = new EnumMap<>(Map.of(DayOfWeek.MONDAY, "mon"));
        final Point point = new Point(1, 2);
        final Date date = new Date(42);
        final List<String> empty = List.of();
        final Map<String, Integer> accessOrdered = new LinkedHashMap<>(16, 0.75f, true);
        // JDK classes without a model, copied by fields: Unsafe is used where reflection cannot access
        final TreeMap<String, Integer> sorted = new TreeMap<>(Map.of("m", 4, "n", 5));
        final StringBuilder text = new StringBuilder("abc");

        Graph() {
            accessOrdered.put("k", 1);
            accessOrdered.put("l", 2);
            accessOrdered.get("k");
            immutableSet = Set.of(key);
            immutableMap = Map.of(key, "j");
        }

    }

    public static void main(String[] args) {
        Graph original = new Graph();
        Graph clone = Cloners.reflection().clone(original);
        check(clone != original, "graph is copied");
        check(clone.list.equals(original.list) && clone.list != original.list, "ArrayList");
        check(clone.linked.equals(original.linked), "LinkedList");
        check(List.copyOf(clone.deque).equals(List.copyOf(original.deque)), "ArrayDeque");
        check(clone.map.equals(original.map), "HashMap");
        check(clone.set.equals(original.set), "HashSet");
        check(clone.immutableList.equals(original.immutableList), "List.of");
        check(clone.key != original.key && clone.immutableSet.contains(clone.key), "Set.of with identity key");
        check("j".equals(clone.immutableMap.get(clone.key)), "Map.of with identity key");
        check("i".equals(clone.identity.get("h")), "IdentityHashMap");
        check("mon".equals(clone.days.get(DayOfWeek.MONDAY)), "EnumMap");
        check(clone.point.equals(original.point), "record");
        check(clone.date.equals(original.date) && clone.date != original.date, "Date");
        check(clone.empty == original.empty, "empty List.of keeps identity");
        clone.accessOrdered.get("l");
        check(List.of("k", "l").equals(List.copyOf(clone.accessOrdered.keySet())), "LinkedHashMap keeps access order");
        check(clone.sorted.equals(original.sorted) && clone.sorted != original.sorted, "TreeMap");
        check("abc".contentEquals(clone.text) && clone.text != original.text, "StringBuilder");
        System.out.println("Cloning without --add-opens works.");
    }

    private static void check(boolean condition, String what) {
        if (!condition) {
            throw new IllegalStateException("Wrong clone: " + what);
        }
        Objects.requireNonNull(what);
    }

    private NoOpensCheck() {
    }

}
