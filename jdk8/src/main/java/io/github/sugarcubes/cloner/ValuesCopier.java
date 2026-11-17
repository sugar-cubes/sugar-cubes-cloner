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

import io.github.sugarcubes.cloner.internal.ValuesModel;

/**
 * Copier of objects created from the values of their components, e.g. records. Gets from the low-level
 * {@link ValuesModel} how to read the values and to create an instance, and decides itself what to copy: copies the
 * values completely before creating the clone, so the constructor may validate or copy them.
 *
 * <p>An object may be part of a reference cycle, e.g. a record may contain itself in a list component. Then the list
 * cannot be completed before the clone exists, and the constructor gets it incomplete. This works if the constructor
 * keeps the component as is: the list is completed after the clone is registered. If the constructor replaces or
 * reads it (defensive copy, validation), the object cannot be cloned and {@link ClonerException} is thrown.</p>
 *
 * @param <T> type
 * @author Maxim Butov
 */
public class ValuesCopier<T> implements ObjectCopier<T> {

    private final ValuesModel<T> model;

    private final boolean shallow;

    /**
     * Creates the copier.
     *
     * @param model low-level model of the type
     * @param shallow shallow mode: values are passed to the constructor as is
     */
    public ValuesCopier(ValuesModel<T> model, boolean shallow) {
        this.model = model;
        this.shallow = shallow;
    }

    @Override
    public T copy(T original, CopyContext context) throws Exception {
        Object[] values = model.read(original);
        if (shallow) {
            return context.register(model.create(values));
        }
        Object[] copies = new Object[values.length];
        for (int k = 0; k < values.length; k++) {
            copies[k] = context.copyComplete(values[k]);
        }
        boolean[] incomplete = new boolean[values.length];
        boolean anyIncomplete = false;
        for (int k = 0; k < values.length; k++) {
            incomplete[k] = !context.isComplete(values[k]);
            anyIncomplete |= incomplete[k];
        }
        if (!anyIncomplete) {
            return context.register(model.create(copies));
        }
        return context.register(createWithIncomplete(original, copies, incomplete));
    }

    /**
     * Creates the clone from values some of which are not complete yet because of a reference cycle, and checks that
     * the constructor keeps them as is.
     */
    private T createWithIncomplete(T original, Object[] copies, boolean[] incomplete) {
        T clone;
        try {
            clone = model.create(copies);
        }
        catch (ClonerException e) {
            throw cycleError(original, "its constructor fails", e.getCause());
        }
        Object[] kept = model.read(clone);
        String[] names = model.getNames();
        for (int k = 0; k < copies.length; k++) {
            if (incomplete[k] && kept[k] != copies[k]) {
                throw cycleError(original, String.format("its constructor replaces the component '%s'", names[k]), null);
            }
        }
        return clone;
    }

    private static ClonerException cycleError(Object original, String reason, Throwable cause) {
        return new ClonerException(String.format("Cannot clone %s: it is part of a reference cycle, so some of its "
            + "components are not completely copied when the clone is created, and %s.",
            original.getClass().getName(), reason), cause);
    }

}
