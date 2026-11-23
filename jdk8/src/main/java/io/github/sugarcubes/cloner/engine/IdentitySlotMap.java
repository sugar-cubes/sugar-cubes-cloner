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
 * Slot map for one thread: open addressing with linear probing over parallel arrays of identity hash codes and
 * slots. A probe compares hash codes only, a slot is read when its hash code matches, so a lookup touches one array
 * and allocates nothing.
 */
final class IdentitySlotMap implements SlotMap {

    /**
     * Default capacity, a power of two.
     */
    private static final int DEFAULT_CAPACITY = 32;

    /**
     * Shared tables of an empty map, never written: the first insertion always resizes them.
     */
    private static final int[] EMPTY_HASHES = new int[1];
    private static final CloneSlot[] EMPTY_SLOTS = new CloneSlot[1];

    /**
     * Identity hash codes with the high bit set, zero marks an empty cell.
     */
    private int[] hashes = EMPTY_HASHES;

    private CloneSlot[] slots = EMPTY_SLOTS;

    private int size;

    /**
     * Length of the tables created on the first insertion.
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
        initialLength = capacity;
    }

    private static int hash(Object original) {
        return System.identityHashCode(original) | Integer.MIN_VALUE;
    }

    /**
     * Finds the index of the original or of the empty cell where it should be.
     */
    private int indexOf(Object original, int hash) {
        int[] hashes = this.hashes;
        int mask = hashes.length - 1;
        int i = hash & mask;
        while (true) {
            int h = hashes[i];
            if (h == 0 || h == hash && slots[i].original == original) {
                return i;
            }
            i = (i + 1) & mask;
        }
    }

    @Override
    public CloneSlot get(Object original) {
        return slots[indexOf(original, hash(original))];
    }

    @Override
    public CloneSlot getOrCreate(Object original, Function<Object, CloneSlot> factory) {
        int hash = hash(original);
        int i = indexOf(original, hash);
        CloneSlot slot = slots[i];
        if (slot == null) {
            slot = factory.apply(original);
            insert(i, hash, slot);
        }
        return slot;
    }

    @Override
    public void put(CloneSlot slot) {
        int hash = hash(slot.original);
        int i = indexOf(slot.original, hash);
        if (slots[i] != null) {
            slots[i] = slot;
        }
        else {
            insert(i, hash, slot);
        }
    }

    private void insert(int index, int hash, CloneSlot slot) {
        int newSize = size + 1;
        // keeps the load factor at most 2/3; the empty tables are always replaced
        if (newSize + (newSize << 1) > hashes.length << 1) {
            resize();
            index = indexOf(slot.original, hash);
        }
        hashes[index] = hash;
        slots[index] = slot;
        size = newSize;
    }

    /**
     * Doubles the tables; the originals are distinct, so they are placed by their hash codes alone.
     */
    private void resize() {
        int[] oldHashes = hashes;
        CloneSlot[] oldSlots = slots;
        int length = oldHashes == EMPTY_HASHES ? initialLength : oldHashes.length * 2;
        int[] newHashes = new int[length];
        CloneSlot[] newSlots = new CloneSlot[length];
        int mask = length - 1;
        for (int j = 0; j < oldHashes.length; j++) {
            int h = oldHashes[j];
            if (h != 0) {
                int i = h & mask;
                while (newHashes[i] != 0) {
                    i = (i + 1) & mask;
                }
                newHashes[i] = h;
                newSlots[i] = oldSlots[j];
            }
        }
        hashes = newHashes;
        slots = newSlots;
    }

}
