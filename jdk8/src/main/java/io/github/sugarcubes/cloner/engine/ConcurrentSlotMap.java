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

import java.util.function.Function;

/**
 * Slot map for several threads: {@link IdentitySlotMap} segments, each guarded by its own lock, so a lookup allocates
 * nothing and threads rarely wait for each other. A segment is chosen by the high bits of the identity hash code, the
 * segment itself uses the low ones.
 */
final class ConcurrentSlotMap implements SlotMap {

    /**
     * Number of segments, a power of two.
     */
    private static final int SEGMENTS = 32;

    /**
     * Shift which leaves the bits of a segment index.
     */
    private static final int SHIFT = Integer.SIZE - Integer.numberOfTrailingZeros(SEGMENTS);

    /**
     * Initial capacity of a segment, a power of two.
     */
    private static final int SEGMENT_CAPACITY = 4;

    /**
     * Multiplier of the Fibonacci hashing: spreads the identity hash code over the high bits.
     */
    private static final int GOLDEN_RATIO = 0x9E3779B9;

    private final IdentitySlotMap[] segments = new IdentitySlotMap[SEGMENTS];

    /**
     * Creates the map.
     */
    ConcurrentSlotMap() {
        for (int i = 0; i < SEGMENTS; i++) {
            segments[i] = new IdentitySlotMap(SEGMENT_CAPACITY);
        }
    }

    private IdentitySlotMap segment(Object original) {
        return segments[(System.identityHashCode(original) * GOLDEN_RATIO) >>> SHIFT];
    }

    @Override
    public CloneSlot get(Object original) {
        IdentitySlotMap segment = segment(original);
        synchronized (segment) {
            return segment.get(original);
        }
    }

    @Override
    public CloneSlot getOrCreate(Object original, Function<Object, CloneSlot> factory) {
        IdentitySlotMap segment = segment(original);
        synchronized (segment) {
            return segment.getOrCreate(original, factory);
        }
    }

    @Override
    public void put(CloneSlot slot) {
        IdentitySlotMap segment = segment(slot.original);
        synchronized (segment) {
            segment.put(slot);
        }
    }

}
