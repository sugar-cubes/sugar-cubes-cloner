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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.github.sugarcubes.cloner.internal.CompoundInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.FieldAdapterFactory;
import io.github.sugarcubes.cloner.internal.FieldAdapterFieldCopierFactory;
import io.github.sugarcubes.cloner.internal.InstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.ObjenesisInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.Reflection;
import io.github.sugarcubes.cloner.internal.ReflectionImpl$Jdk8;
import io.github.sugarcubes.cloner.internal.ReflectionInstanceAllocatorFactory;

/**
 * Builder for {@link Toolset}. Tools which are not set get failsafe defaults: plain reflection, Objenesis (if
 * available) or constructor allocator, reflection field copier.
 *
 * @author Maxim Butov
 */
public class ToolsetBuilder {

    private Reflection reflection;
    private List<InstanceAllocatorFactory> allocators = Collections.emptyList();
    private FieldCopierFactory fieldCopier;

    /**
     * Sets reflection.
     *
     * @param reflection reflection
     * @return same builder instance
     */
    public ToolsetBuilder reflection(Reflection reflection) {
        this.reflection = reflection;
        return this;
    }

    /**
     * Sets instance allocator factory.
     *
     * @param allocator instance allocator factory
     * @return same builder instance
     */
    public ToolsetBuilder allocator(InstanceAllocatorFactory allocator) {
        this.allocators = Collections.singletonList(allocator);
        return this;
    }

    /**
     * Sets instance allocator factories. For every type the first allocator which succeeds is used.
     *
     * @param allocator instance allocator factories in order of preference
     * @return same builder instance
     */
    public ToolsetBuilder allocators(InstanceAllocatorFactory... allocator) {
        this.allocators = Arrays.asList(allocator);
        return this;
    }

    /**
     * Sets field copier factory.
     *
     * @param fieldCopier field copier factory
     * @return same builder instance
     */
    public ToolsetBuilder fieldCopier(FieldCopierFactory fieldCopier) {
        this.fieldCopier = fieldCopier;
        return this;
    }

    /**
     * Sets field copier factory which uses field adapters.
     *
     * @param fieldAdapter field adapter factory
     * @return same builder instance
     */
    public ToolsetBuilder fieldAdapter(FieldAdapterFactory fieldAdapter) {
        return fieldCopier(new FieldAdapterFieldCopierFactory(fieldAdapter));
    }

    /**
     * Creates toolset.
     *
     * @return toolset
     */
    public Toolset build() {
        Reflection reflection = this.reflection != null ? this.reflection : new ReflectionImpl$Jdk8();
        FieldCopierFactory fieldCopier = this.fieldCopier != null ? this.fieldCopier : new ReflectionFieldCopierFactory();
        return new Toolset(reflection, buildAllocatorFactory(reflection), fieldCopier);
    }

    private InstanceAllocatorFactory buildAllocatorFactory(Reflection reflection) {
        if (allocators.isEmpty()) {
            return ObjenesisInstanceAllocatorFactory.isAvailable()
                ? new ObjenesisInstanceAllocatorFactory()
                : new ReflectionInstanceAllocatorFactory(reflection);
        }
        if (allocators.size() == 1) {
            return allocators.get(0);
        }
        return new CompoundInstanceAllocatorFactory(allocators);
    }

}
