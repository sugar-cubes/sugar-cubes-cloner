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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Collections and maps are rebuilt from copied elements, hash codes of copied keys are computed anew.
 */
class ContainerCopyingTest {

    /**
     * Key with equals and hash code by its own mutable state.
     */
    static class ValueKey {

        String name;

        ValueKey(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ValueKey && Objects.equals(((ValueKey) obj).name, name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }

    }

    /**
     * Key with identity hash code.
     */
    static class IdentityKey {
    }

    static class Holder {

        final ValueKey valueKey = new ValueKey("a");
        final IdentityKey identityKey = new IdentityKey();
        final Map<ValueKey, String> byValue = new HashMap<>();
        final Set<ValueKey> valueSet = new HashSet<>();
        final Map<IdentityKey, String> byIdentity = new LinkedHashMap<>();
        final Set<IdentityKey> identitySet = new HashSet<>();
        final Map<Object, Object> table = new Hashtable<>();

        Holder() {
            byValue.put(valueKey, "value");
            valueSet.add(valueKey);
            byIdentity.put(new IdentityKey(), "first");
            byIdentity.put(identityKey, "second");
            identitySet.add(identityKey);
            table.put(identityKey, valueKey);
        }

    }

    @ParameterizedTest
    @EnumSource(CloningMode.class)
    void keysWithHashCodeByState(CloningMode mode) {
        Holder clone = Cloners.builder().mode(mode).build().clone(new Holder());

        Assertions.assertEquals("value", clone.byValue.get(clone.valueKey));
        Assertions.assertTrue(clone.valueSet.contains(clone.valueKey));
        Assertions.assertEquals("value", clone.byValue.get(new ValueKey("a")));
    }

    @ParameterizedTest
    @EnumSource(CloningMode.class)
    void keysWithIdentityHashCode(CloningMode mode) {
        Holder clone = Cloners.builder().mode(mode).build().clone(new Holder());

        Assertions.assertEquals("second", clone.byIdentity.get(clone.identityKey));
        Assertions.assertEquals(List.of("first", "second"), new ArrayList<>(clone.byIdentity.values()));
        Assertions.assertTrue(clone.identitySet.contains(clone.identityKey));
        Assertions.assertSame(clone.valueKey, clone.table.get(clone.identityKey));
    }

    @ParameterizedTest
    @EnumSource(CloningMode.class)
    void accessOrderOfLinkedHashMap(CloningMode mode) {
        Map<String, Integer> original = new LinkedHashMap<>(16, 0.75f, true);
        original.put("a", 1);
        original.put("b", 2);
        original.put("c", 3);
        original.get("a");

        Map<String, Integer> clone = Cloners.builder().mode(mode).build().clone(original);
        clone.get("b");

        Assertions.assertEquals(List.of("b", "c", "a"), new ArrayList<>(original.keySet()));
        Assertions.assertEquals(List.of("c", "a", "b"), new ArrayList<>(clone.keySet()));
    }

    @ParameterizedTest
    @EnumSource(CloningMode.class)
    void listContainingItself(CloningMode mode) {
        List<Object> original = new ArrayList<>();
        original.add("a");
        original.add(original);

        List<Object> clone = Cloners.builder().mode(mode).build().clone(original);

        Assertions.assertNotSame(original, clone);
        Assertions.assertEquals("a", clone.get(0));
        Assertions.assertSame(clone, clone.get(1));
    }

}
