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
import io.github.sugarcubes.cloner.internal.ObjenesisInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.Reflection;
import io.github.sugarcubes.cloner.internal.ReflectionImpl$Jdk8;
import io.github.sugarcubes.cloner.internal.ReflectionInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.UnsafeBridge;

/**
 * todo
 */
public class Toolset {

    public static Toolset failsafe() {
        ReflectionImpl$Jdk8 reflection = new ReflectionImpl$Jdk8();

        InstanceAllocatorFactory allocatorFactory = ObjenesisInstanceAllocatorFactory.isAvailable()
            ? new ObjenesisInstanceAllocatorFactory()
            : new ReflectionInstanceAllocatorFactory(reflection);

        return new Toolset(reflection, allocatorFactory, new ReflectionFieldCopierFactory());
    }

    private final Reflection reflection;
    private final InstanceAllocatorFactory allocatorFactory;
    private final FieldCopierFactory fieldCopierFactory;

    public Toolset(Reflection reflection, InstanceAllocatorFactory allocatorFactory, FieldCopierFactory fieldCopierFactory) {
        this.reflection = reflection;
        this.allocatorFactory = allocatorFactory;
        this.fieldCopierFactory = fieldCopierFactory;
    }

    public Toolset(Reflection reflection, InstanceAllocatorFactory allocatorFactory, FieldAdapterFactory fieldAdapterFactory) {
        this(reflection, allocatorFactory, new FieldAdapterFieldCopierFactory(fieldAdapterFactory));
    }

    public Reflection getReflection() {
        return reflection;
    }

    public InstanceAllocatorFactory getAllocatorFactory() {
        return allocatorFactory;
    }

    public FieldCopierFactory getFieldCopierFactory() {
        return fieldCopierFactory;
    }

    public UnsafeBridge getUnsafe() {
        return null;
    }

}
