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
package io.github.sugarcubes.cloner.spi;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

/**
 * Low-level model of a type whose instances are created without constructor and whose state is its fields. Knows how
 * to allocate an instance and to read and write the fields declared by the type itself; what and how to copy is
 * decided by the caller.
 *
 * @param <T> type
 * @author Maxim Butov
 */
public final class FieldsModel<T> {

    private final Class<T> type;

    private final Optional<InstanceAllocator<T>> allocator;

    private final Field[] fields;

    private final FieldAdapterFactory adapters;

    /**
     * Creates the model of the type.
     *
     * @param type type
     * @param reflection reflection to list fields
     * @param allocators instance allocator factory
     * @param adapters field adapter factory
     */
    public FieldsModel(Class<T> type, Reflection reflection, InstanceAllocatorFactory allocators, FieldAdapterFactory adapters) {
        this.type = type;
        this.allocator = allocators.newAllocator(type);
        this.fields = Arrays.stream(reflection.getDeclaredFields(type))
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .toArray(Field[]::new);
        this.adapters = adapters;
    }

    /**
     * Returns the type.
     *
     * @return type
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * Returns allocator of instances.
     *
     * @return allocator or empty if no allocator supports the type (e.g. an abstract class)
     */
    public Optional<InstanceAllocator<T>> getAllocator() {
        return allocator;
    }

    /**
     * Returns instance fields declared by the type itself, without fields of super types.
     *
     * @return fields
     */
    public Field[] getFields() {
        return fields.clone();
    }

    /**
     * Creates an adapter which reads and writes the field.
     *
     * @param field one of {@link #getFields()}
     * @return field adapter, or empty if the field cannot be accessed
     */
    public Optional<FieldAdapter> adapter(Field field) {
        return adapters.newAdapter(field);
    }

}
