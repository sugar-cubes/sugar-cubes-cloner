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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import io.github.sugarcubes.cloner.util.IdentityReference;

/**
 * Slot map for several threads: {@link ConcurrentHashMap} with {@link IdentityReference} keys.
 */
final class ConcurrentSlotMap implements SlotMap {

    private final Map<IdentityReference<Object>, CloneSlot> map = new ConcurrentHashMap<>();

    @Override
    public CloneSlot get(Object original) {
        return map.get(new IdentityReference<>(original));
    }

    @Override
    public CloneSlot getOrCreate(Object original, Function<Object, CloneSlot> factory) {
        IdentityReference<Object> key = new IdentityReference<>(original);
        CloneSlot slot = map.get(key);
        return slot != null ? slot : map.computeIfAbsent(key, factory);
    }

    @Override
    public void put(Object original, CloneSlot slot) {
        map.put(new IdentityReference<>(original), slot);
    }

}
