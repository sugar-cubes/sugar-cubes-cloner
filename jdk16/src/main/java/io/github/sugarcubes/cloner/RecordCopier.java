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
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

/**
 * Records copier.
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
        Object[] values = new Object[accessors.length];
        for (int k = 0; k < values.length; k++) {
            values[k] = context.copy(accessors[k].invoke(original));
        }
        return context.register(constructor.newInstance(values));
    }

}
