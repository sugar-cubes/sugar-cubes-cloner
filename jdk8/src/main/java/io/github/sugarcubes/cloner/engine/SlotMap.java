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
 * Identity map from original objects to their {@link CloneSlot slots}; a slot holds its original, so the slot is the
 * whole entry.
 */
interface SlotMap {

    /**
     * Returns the slot of the original.
     *
     * @param original original object
     * @return slot or {@code null} if the original has no slot
     */
    CloneSlot get(Object original);

    /**
     * Returns the slot of the original, creating it if the original has no slot.
     *
     * @param original original object
     * @param factory creates a new slot for the original
     * @return slot
     */
    CloneSlot getOrCreate(Object original, Function<Object, CloneSlot> factory);

    /**
     * Puts the slot, replacing the slot of the same original; for predefined clones, before copying starts.
     *
     * @param slot slot
     */
    void put(CloneSlot slot);

}
