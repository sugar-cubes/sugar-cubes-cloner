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

import java.util.function.Supplier;

import io.github.sugarcubes.cloner.ObjectCopier;
import io.github.sugarcubes.cloner.TwoPhaseObjectCopier;
import io.github.sugarcubes.cloner.spi.ValuesModel;

/**
 * Chooses the copier of an immutable set or map created from its elements (e.g. {@code Set.of}, {@code Map.of}). If
 * hash codes of all keys are known at once, the collection is created anew from copied elements by
 * {@link ValuesCopier}; otherwise it is copied by fields, keeping the hash codes of the original keys, which are right
 * for keys hashed by their state.
 *
 * @param <T> collection type
 * @author Maxim Butov
 */
public class HashValuesCopier<T> extends ChoosingCopier<T> {

    private final ValuesCopier<T> values;

    private final Supplier<TwoPhaseObjectCopier<T>> fields;

    private final ValuesModel<T> model;

    private final boolean shallow;

    /**
     * Creates the copier.
     *
     * @param model low-level model of the collection
     * @param fields copier of the collection fields, needed only for keys hashed by their state
     * @param shallow shallow mode: keys are the original ones
     */
    public HashValuesCopier(ValuesModel<T> model, Supplier<TwoPhaseObjectCopier<T>> fields, boolean shallow) {
        this.values = new ValuesCopier<>(model, shallow);
        this.fields = fields;
        this.model = model;
        this.shallow = shallow;
    }

    @Override
    public ObjectCopier<T> choose(T original) {
        return shallow || HashKeys.knownAtOnce(model.read(original), model.isMap()) ? values : fields.get();
    }

}
