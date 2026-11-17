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

import java.util.Optional;

import io.github.sugarcubes.cloner.internal.ContainerModel;
import io.github.sugarcubes.cloner.internal.ContainerModels;
import io.github.sugarcubes.cloner.internal.FieldAdapterFactory;
import io.github.sugarcubes.cloner.internal.FieldsModel;
import io.github.sugarcubes.cloner.internal.InstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.Reflection;
import io.github.sugarcubes.cloner.internal.ValuesModel;
import io.github.sugarcubes.cloner.internal.ValuesModelProvider;

/**
 * Low-level tools of the reflection cloner: reflection, instance allocator and field adapters. The cloner asks them
 * how to create an instance and to read and write its fields, everything else is decided by the cloner.
 *
 * @author Maxim Butov
 * @see ToolsetBuilder
 */
public class Toolset {

    /**
     * Creates toolset which works on any JVM without additional permissions.
     *
     * @return failsafe toolset
     */
    public static Toolset failsafe() {
        return new ToolsetBuilder().build();
    }

    private final Reflection reflection;
    private final InstanceAllocatorFactory allocatorFactory;
    private final FieldAdapterFactory fieldAdapterFactory;
    private final ValuesModelProvider valuesModels = new ValuesModelProvider();
    private final ContainerModels containerModels;

    /**
     * Constructor.
     *
     * @param reflection reflection
     * @param allocatorFactory instance allocator factory
     * @param fieldAdapterFactory field adapter factory
     */
    public Toolset(Reflection reflection, InstanceAllocatorFactory allocatorFactory, FieldAdapterFactory fieldAdapterFactory) {
        this.reflection = reflection;
        this.allocatorFactory = allocatorFactory;
        this.fieldAdapterFactory = fieldAdapterFactory;
        this.containerModels = new ContainerModels(reflection);
    }

    /**
     * Returns reflection.
     *
     * @return reflection
     */
    public Reflection getReflection() {
        return reflection;
    }

    /**
     * Returns instance allocator factory.
     *
     * @return instance allocator factory
     */
    public InstanceAllocatorFactory getAllocatorFactory() {
        return allocatorFactory;
    }

    /**
     * Returns field adapter factory.
     *
     * @return field adapter factory
     */
    public FieldAdapterFactory getFieldAdapterFactory() {
        return fieldAdapterFactory;
    }

    /**
     * Creates the low-level model of a type whose state is its fields.
     *
     * @param <T> type
     * @param type type
     * @return model of the type
     */
    public <T> FieldsModel<T> fieldsModel(Class<T> type) {
        return new FieldsModel<>(type, reflection, allocatorFactory, fieldAdapterFactory);
    }

    /**
     * Creates the low-level model of a type whose instances are created from the values of their components.
     *
     * @param <T> type
     * @param type type
     * @return model of the type, or empty if the type is not such (e.g. not a record)
     */
    public <T> Optional<ValuesModel<T>> valuesModel(Class<T> type) {
        return valuesModels.valuesModel(type, reflection);
    }

    /**
     * Returns the low-level model of a container type (a well-known collection or map).
     *
     * @param <T> type
     * @param type type
     * @return model of the type, or empty if the type is not a supported container
     */
    public <T> Optional<ContainerModel<T>> containerModel(Class<T> type) {
        return containerModels.containerModel(type);
    }

}
