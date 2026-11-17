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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.sugarcubes.cloner.internal.CompoundInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.ObjenesisInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.ReflectionFieldAdapterImpl;
import io.github.sugarcubes.cloner.internal.ReflectionImpl$Jdk8;
import io.github.sugarcubes.cloner.internal.ReflectionInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.spi.FieldAdapterFactory;
import io.github.sugarcubes.cloner.spi.InstanceAllocatorFactory;
import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.util.Checks;

/**
 * Builder for {@link Toolset}. Tools which are not set get failsafe defaults: plain reflection, Objenesis (if
 * available) as instance allocator, constructor without parameters as fallback, reflection field adapters.
 *
 * <p>Instance allocators create objects without calling constructors, as {@link Object#clone()} does. Constructors
 * may have side effects (registration, counters, validation) which a clone should not repeat, so allocators calling
 * constructors are accepted only as an explicit {@link #constructorFallback(InstanceAllocatorFactory) fallback}
 * for types which no allocator supports.</p>
 *
 * @author Maxim Butov
 */
public class ToolsetBuilder {

    private Reflection reflection;
    private List<InstanceAllocatorFactory> allocators;
    private InstanceAllocatorFactory constructorFallback;
    private boolean constructorFallbackDisabled;
    private FieldAdapterFactory fieldAdapter;

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
     * Sets instance allocator factory, which must not call constructors.
     *
     * @param allocator instance allocator factory
     * @return same builder instance
     */
    public ToolsetBuilder allocator(InstanceAllocatorFactory allocator) {
        return allocators(allocator);
    }

    /**
     * Sets instance allocator factories, which must not call constructors. For every type the first factory
     * supporting it is used.
     *
     * @param allocators instance allocator factories in order of preference
     * @return same builder instance
     */
    public ToolsetBuilder allocators(InstanceAllocatorFactory... allocators) {
        for (InstanceAllocatorFactory allocator : allocators) {
            Checks.argNotNull(allocator, "Instance allocator factory");
            Checks.illegalArg(allocator.invokesConstructor(),
                "%s calls constructors, set it as constructor fallback.", allocator);
        }
        this.allocators = Arrays.asList(allocators);
        return this;
    }

    /**
     * Sets instance allocator factory for types which no allocator supports. It may call constructors.
     *
     * @param constructorFallback instance allocator factory
     * @return same builder instance
     */
    public ToolsetBuilder constructorFallback(InstanceAllocatorFactory constructorFallback) {
        this.constructorFallback = Checks.argNotNull(constructorFallback, "Constructor fallback");
        this.constructorFallbackDisabled = false;
        return this;
    }

    /**
     * Disables constructor fallback: types which no allocator supports cannot be cloned.
     *
     * @return same builder instance
     */
    public ToolsetBuilder noConstructorFallback() {
        this.constructorFallback = null;
        this.constructorFallbackDisabled = true;
        return this;
    }

    /**
     * Sets field adapter factory.
     *
     * @param fieldAdapter field adapter factory
     * @return same builder instance
     */
    public ToolsetBuilder fieldAdapter(FieldAdapterFactory fieldAdapter) {
        this.fieldAdapter = fieldAdapter;
        return this;
    }

    /**
     * Creates toolset.
     *
     * @return toolset
     */
    public Toolset build() {
        Reflection reflection = this.reflection != null ? this.reflection : new ReflectionImpl$Jdk8();
        FieldAdapterFactory fieldAdapter =
            this.fieldAdapter != null ? this.fieldAdapter : ReflectionFieldAdapterImpl.factory(reflection);
        return new Toolset(reflection, buildAllocatorFactory(reflection), fieldAdapter);
    }

    private InstanceAllocatorFactory buildAllocatorFactory(Reflection reflection) {
        List<InstanceAllocatorFactory> factories = new ArrayList<>();
        if (allocators != null) {
            factories.addAll(allocators);
        }
        else if (ObjenesisInstanceAllocatorFactory.isAvailable()) {
            factories.add(new ObjenesisInstanceAllocatorFactory());
        }
        if (!constructorFallbackDisabled) {
            factories.add(
                constructorFallback != null ? constructorFallback : new ReflectionInstanceAllocatorFactory(reflection));
        }
        if (factories.isEmpty()) {
            throw new IllegalStateException("No instance allocators: set allocators or enable constructor fallback.");
        }
        return factories.size() == 1 ? factories.get(0) : new CompoundInstanceAllocatorFactory(factories);
    }

}
