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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

/**
 * Records copier. Creates the clone with the canonical constructor from completely copied components.
 *
 * <p>A record may be part of a reference cycle, e.g. contain itself in a list component. Then the list cannot be
 * completed before the record clone exists, and the constructor gets it incomplete. This works if the constructor
 * keeps the component as is: the list is completed after the clone is registered. If the constructor replaces or
 * reads it (defensive copy, validation), the record cannot be cloned and {@link ClonerException} is thrown.</p>
 *
 * @param <T> type
 * @author Maxim Butov
 */
public class RecordCopier<T extends Record> implements ObjectCopier<T> {

    /**
     * Record components accessors.
     */
    private final Method[] accessors;

    /**
     * Record constructor.
     */
    private final Constructor<T> constructor;

    /**
     * Constructor.
     *
     * @param type record type
     */
    public RecordCopier(Class<T> type) {
        Checks.illegalArg(!type.isRecord(), "%s is not a record.", type);
        RecordComponent[] components = type.getRecordComponents();
        accessors = Arrays.stream(components)
            .map(RecordComponent::getAccessor)
            .toArray(Method[]::new);
        Class<?>[] componentTypes = Arrays.stream(components)
            .map(RecordComponent::getType)
            .toArray(Class<?>[]::new);
        constructor = ReflectionUtils.getConstructor(type, componentTypes);
    }

    @Override
    public T copy(T original, CopyContext context) throws Exception {
        Object[] components = new Object[accessors.length];
        Object[] values = new Object[accessors.length];
        boolean[] incomplete = new boolean[accessors.length];
        boolean anyIncomplete = false;
        for (int k = 0; k < values.length; k++) {
            components[k] = accessors[k].invoke(original);
            values[k] = context.copyComplete(components[k]);
        }
        for (int k = 0; k < values.length; k++) {
            incomplete[k] = !context.isComplete(components[k]);
            anyIncomplete |= incomplete[k];
        }
        if (!anyIncomplete) {
            return context.register(constructor.newInstance(values));
        }
        T clone;
        try {
            clone = constructor.newInstance(values);
        }
        catch (InvocationTargetException e) {
            throw cycleError(original, "its canonical constructor fails", e.getCause());
        }
        for (int k = 0; k < values.length; k++) {
            if (incomplete[k] && accessors[k].invoke(clone) != values[k]) {
                throw cycleError(original, String.format("its canonical constructor replaces the component '%s'",
                    accessors[k].getName()), null);
            }
        }
        return context.register(clone);
    }

    private static ClonerException cycleError(Object original, String reason, Throwable cause) {
        return new ClonerException(String.format("Cannot clone %s: it is part of a reference cycle, so some of its "
            + "components are not completely copied when the clone is created, and %s.",
            original.getClass().getName(), reason), cause);
    }

}
