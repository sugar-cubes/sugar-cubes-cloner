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

import io.github.sugarcubes.cloner.spi.FieldAdapterFactory;
import io.github.sugarcubes.cloner.spi.InstanceAllocatorFactory;
import io.github.sugarcubes.cloner.spi.Reflection;

/**
 * Low-level tools of the reflection cloner: reflection, instance allocators and field adapters. The cloner asks them
 * how to create an instance and to read and write its fields, everything else is decided by the cloner.
 *
 * <p>Toolsets are created by the profiles of {@link Toolsets} or by {@link ToolsetBuilder}.</p>
 *
 * @author Maxim Butov
 */
public final class Toolset {

    private final Reflection reflection;
    private final InstanceAllocatorFactory allocatorFactory;
    private final FieldAdapterFactory fieldAdapterFactory;
    private final String description;

    Toolset(Reflection reflection, InstanceAllocatorFactory allocatorFactory, FieldAdapterFactory fieldAdapterFactory,
        String description) {
        this.reflection = reflection;
        this.allocatorFactory = allocatorFactory;
        this.fieldAdapterFactory = fieldAdapterFactory;
        this.description = description;
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
     * Describes the chosen mechanisms, e.g. for logging.
     *
     * @return description of the toolset
     */
    @Override
    public String toString() {
        return description;
    }

}
