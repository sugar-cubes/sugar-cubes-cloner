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

import java.util.function.Function;

/**
 * Slot map for one thread: open addressing with linear probing over one array of keys and values, as in
 * {@link java.util.IdentityHashMap}, so a lookup allocates nothing.
 */
final class IdentitySlotMap implements SlotMap {

    /**
     * Default capacity, a power of two.
     */
    private static final int DEFAULT_CAPACITY = 32;

    /**
     * Shared table of an empty map: the smallest one, never written, the first insertion replaces it.
     */
    private static final Object[] EMPTY = new Object[2];

    /**
     * Keys at even indices, values at the following odd ones; the length is twice the capacity.
     */
    private Object[] table = EMPTY;

    private int size;

    /**
     * Length of the table created on the first insertion.
     */
    private final int initialLength;

    /**
     * Creates the map with the default capacity.
     */
    IdentitySlotMap() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Creates the map with the capacity.
     *
     * @param capacity initial capacity, a power of two
     */
    IdentitySlotMap(int capacity) {
        initialLength = capacity * 2;
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private static int hash(Object key, int length) {
        // same as IdentityHashMap: an even index, low bits spread
        int h = System.identityHashCode(key);
        return ((h << 1) - (h << 8)) & (length - 1);
    }

    /**
     * Finds the index of the key or of the empty cell where it should be.
     */
    private static int indexOf(Object[] table, Object key) {
        int length = table.length;
        int i = hash(key, length);
        while (true) {
            Object item = table[i];
            if (item == key || item == null) {
                return i;
            }
            i = i + 2 < length ? i + 2 : 0;
        }
    }

    @Override
    public CloneSlot get(Object original) {
        Object[] table = this.table;
        return (CloneSlot) table[indexOf(table, original) + 1];
    }

    @Override
    public CloneSlot getOrCreate(Object original, Function<Object, CloneSlot> factory) {
        Object[] table = this.table;
        int i = indexOf(table, original);
        if (table[i] != null) {
            return (CloneSlot) table[i + 1];
        }
        CloneSlot slot = factory.apply(original);
        insert(i, original, slot);
        return slot;
    }

    @Override
    public void put(Object original, CloneSlot slot) {
        int i = indexOf(table, original);
        if (table[i] != null) {
            table[i + 1] = slot;
        }
        else {
            insert(i, original, slot);
        }
    }

    private void insert(int index, Object key, CloneSlot slot) {
        int newSize = size + 1;
        // keeps the load factor at most 2/3; the empty table is always replaced
        if (newSize + (newSize << 1) > table.length) {
            resize();
            index = indexOf(table, key);
        }
        // the value first: a cell with a key always has its value
        table[index + 1] = slot;
        table[index] = key;
        size = newSize;
    }

    private void resize() {
        Object[] oldTable = table;
        Object[] newTable = new Object[oldTable == EMPTY ? initialLength : oldTable.length * 2];
        for (int j = 0; j < oldTable.length; j += 2) {
            Object key = oldTable[j];
            if (key != null) {
                int i = indexOf(newTable, key);
                newTable[i + 1] = oldTable[j + 1];
                newTable[i] = key;
            }
        }
        table = newTable;
    }

}
