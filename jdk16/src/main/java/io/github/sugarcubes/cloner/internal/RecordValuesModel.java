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
package io.github.sugarcubes.cloner.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.util.ClonerExceptionUtils;

/**
 * Values model of a record: components are read with accessors, instances are created with the canonical
 * constructor. Accessors and constructor are made accessible by the reflection, so non-public records work too.
 *
 * @param <T> record type
 * @author Maxim Butov
 */
final class RecordValuesModel<T extends Record> implements ValuesModel<T> {

    private final Class<T> type;

    private final String[] names;

    private final Method[] accessors;

    private final Constructor<T> constructor;

    RecordValuesModel(Class<T> type, Reflection reflection) {
        RecordComponent[] components = type.getRecordComponents();
        this.type = type;
        this.names = Arrays.stream(components).map(RecordComponent::getName).toArray(String[]::new);
        this.accessors = Arrays.stream(components)
            .map(component -> reflection.makeAccessible(component.getAccessor()))
            .toArray(Method[]::new);
        Class<?>[] componentTypes = Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new);
        this.constructor = reflection.makeAccessible(reflection.getDeclaredConstructor(type, componentTypes));
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public String[] getNames() {
        return names.clone();
    }

    @Override
    public Object[] read(T object) {
        Object[] values = new Object[accessors.length];
        for (int k = 0; k < values.length; k++) {
            Method accessor = accessors[k];
            values[k] = ClonerExceptionUtils.replaceException(() -> accessor.invoke(object));
        }
        return values;
    }

    @Override
    public T create(Object[] values) {
        try {
            return constructor.newInstance(values);
        }
        catch (InvocationTargetException e) {
            throw new ClonerException(String.format("Canonical constructor of %s failed.", type.getName()), e.getCause());
        }
        catch (ReflectiveOperationException e) {
            throw new ClonerException(e);
        }
    }

}
