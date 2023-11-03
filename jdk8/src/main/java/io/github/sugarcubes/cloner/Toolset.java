/*
 * Copyright 2017-2023 the original author or authors.
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

import io.github.sugarcubes.cloner.internal.ReflectionImpl$Jdk8;
import io.github.sugarcubes.cloner.internal.FieldAdapterFactory;
import io.github.sugarcubes.cloner.internal.FieldAdapterFieldCopierFactory;
import io.github.sugarcubes.cloner.internal.InstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.Reflection;

/**
 * todo
 */
public class Toolset {

    public static Toolset failsafe() {
        return new Toolset(new ReflectionImpl$Jdk8(), new ReflectionInstanceAllocatorFactory(), new ReflectionFieldCopierFactory());
    }

    private final Reflection reflection;
    private final InstanceAllocatorFactory allocator;
    private final FieldCopierFactory fieldCopier;

    public Toolset(Reflection reflection, InstanceAllocatorFactory allocator, FieldCopierFactory fieldCopier) {
        this.reflection = reflection;
        this.allocator = allocator;
        this.fieldCopier = fieldCopier;
    }

    public Toolset(Reflection reflection, InstanceAllocatorFactory allocator, FieldAdapterFactory fieldAdapter) {
        this(reflection, allocator, new FieldAdapterFieldCopierFactory(fieldAdapter));
    }

    public Reflection getReflection() {
        return reflection;
    }

    public InstanceAllocatorFactory getAllocator() {
        return allocator;
    }

    public FieldCopierFactory getFieldCopier() {
        return fieldCopier;
    }

}
