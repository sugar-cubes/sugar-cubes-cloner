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

import io.github.sugarcubes.cloner.internal.FieldAdapterFactory;
import io.github.sugarcubes.cloner.internal.FieldAdapterFieldCopierFactory;
import io.github.sugarcubes.cloner.internal.InstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.Reflection;

/**
 * Set of low-level tools used by the reflection cloner: reflection, instance allocator and field copier.
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
    private final FieldCopierFactory fieldCopierFactory;

    /**
     * Constructor.
     *
     * @param reflection reflection
     * @param allocatorFactory instance allocator factory
     * @param fieldCopierFactory field copier factory
     */
    public Toolset(Reflection reflection, InstanceAllocatorFactory allocatorFactory, FieldCopierFactory fieldCopierFactory) {
        this.reflection = reflection;
        this.allocatorFactory = allocatorFactory;
        this.fieldCopierFactory = fieldCopierFactory;
    }

    /**
     * Constructor with field copier factory which uses field adapters.
     *
     * @param reflection reflection
     * @param allocatorFactory instance allocator factory
     * @param fieldAdapterFactory field adapter factory
     */
    public Toolset(Reflection reflection, InstanceAllocatorFactory allocatorFactory, FieldAdapterFactory fieldAdapterFactory) {
        this(reflection, allocatorFactory, new FieldAdapterFieldCopierFactory(fieldAdapterFactory));
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
     * Returns field copier factory.
     *
     * @return field copier factory
     */
    public FieldCopierFactory getFieldCopierFactory() {
        return fieldCopierFactory;
    }

}
