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
import java.util.Optional;
import java.util.function.Supplier;

import io.github.sugarcubes.cloner.allocation.CompoundInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.allocation.ObjenesisInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.allocation.ReflectionInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.allocation.UnsafeAllocatorFactory;
import io.github.sugarcubes.cloner.fields.CompoundFieldAdapterFactory;
import io.github.sugarcubes.cloner.fields.NarcissusFieldAdapterImpl;
import io.github.sugarcubes.cloner.fields.ReflectionFieldAdapterImpl;
import io.github.sugarcubes.cloner.fields.UnsafeFieldAdapterImpl;
import io.github.sugarcubes.cloner.platform.JdkPlatform;
import io.github.sugarcubes.cloner.spi.FieldAdapterFactory;
import io.github.sugarcubes.cloner.spi.InstanceAllocatorFactory;
import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.spi.TypeOpener;
import io.github.sugarcubes.cloner.unsafe.UnsafeBridge;
import io.github.sugarcubes.cloner.util.Checks;
import io.github.sugarcubes.cloner.util.Lazy;
import io.github.sugarcubes.cloner.util.NarcissusUtils;
import io.github.sugarcubes.cloner.util.ObjenesisUtils;

/**
 * Builder of {@link Toolset}. Mechanisms are chosen by variants ({@link Allocation}, {@link FieldAccess},
 * {@link ModuleOpening}); the builder creates their implementations for the running JDK and connects them, a variant
 * which is not available fails the build with an explanation. Custom implementations of the {@code spi} interfaces may
 * be set instead.
 *
 * <p>Not set mechanisms are the defaults of {@link Toolsets#defaults()}: no module opening, Objenesis (if available)
 * as instance allocator with constructors as fallback, reflection field access with {@code Unsafe} for fields which
 * reflection cannot access.</p>
 *
 * <p>Instance allocators create objects without calling constructors, as {@link Object#clone()} does. Constructors
 * may have side effects (registration, counters, validation) which a clone should not repeat, so allocators calling
 * constructors are accepted only as an explicit {@link #constructorFallback(InstanceAllocatorFactory) fallback}
 * for types which no allocator supports.</p>
 *
 * @author Maxim Butov
 */
public final class ToolsetBuilder {

    private Reflection reflection;
    private ModuleOpening moduleOpening = ModuleOpening.NONE;
    private List<Allocation> allocation;
    private List<InstanceAllocatorFactory> allocators;
    private InstanceAllocatorFactory constructorFallback;
    private boolean constructorFallbackDisabled;
    private List<FieldAccess> fieldAccess;
    private List<FieldAdapterFactory> fieldAdapters;

    ToolsetBuilder() {
    }

    /**
     * Sets the mechanism opening packages to the cloner for reflection.
     *
     * @param moduleOpening module opening
     * @return same builder instance
     */
    public ToolsetBuilder moduleOpening(ModuleOpening moduleOpening) {
        this.moduleOpening = Checks.argNotNull(moduleOpening, "Module opening");
        return this;
    }

    /**
     * Sets custom reflection instead of the default one, which opens packages by {@link #moduleOpening}.
     *
     * @param reflection reflection
     * @return same builder instance
     */
    public ToolsetBuilder reflection(Reflection reflection) {
        this.reflection = Checks.argNotNull(reflection, "Reflection");
        return this;
    }

    /**
     * Sets the mechanisms creating instances without constructors. For every type the first mechanism supporting it
     * is used.
     *
     * @param allocation mechanisms in order of preference
     * @return same builder instance
     */
    public ToolsetBuilder allocation(Allocation... allocation) {
        this.allocation = Arrays.asList(allocation);
        this.allocators = null;
        return this;
    }

    /**
     * Sets a custom instance allocator factory, which must not call constructors.
     *
     * @param allocator instance allocator factory
     * @return same builder instance
     */
    public ToolsetBuilder allocator(InstanceAllocatorFactory allocator) {
        return allocators(allocator);
    }

    /**
     * Sets custom instance allocator factories, which must not call constructors. For every type the first factory
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
        this.allocation = null;
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
     * Sets the mechanisms reading and writing fields. For every field the first mechanism able to access it is used.
     *
     * @param fieldAccess mechanisms in order of preference
     * @return same builder instance
     */
    public ToolsetBuilder fieldAccess(FieldAccess... fieldAccess) {
        this.fieldAccess = Arrays.asList(fieldAccess);
        this.fieldAdapters = null;
        return this;
    }

    /**
     * Sets a custom field adapter factory.
     *
     * @param fieldAdapter field adapter factory
     * @return same builder instance
     */
    public ToolsetBuilder fieldAdapter(FieldAdapterFactory fieldAdapter) {
        return fieldAdapters(fieldAdapter);
    }

    /**
     * Sets custom field adapter factories. For every field the first factory able to access it is used.
     *
     * @param fieldAdapters field adapter factories in order of preference
     * @return same builder instance
     */
    public ToolsetBuilder fieldAdapters(FieldAdapterFactory... fieldAdapters) {
        for (FieldAdapterFactory fieldAdapter : fieldAdapters) {
            Checks.argNotNull(fieldAdapter, "Field adapter factory");
        }
        this.fieldAdapters = Arrays.asList(fieldAdapters);
        this.fieldAccess = null;
        return this;
    }

    /**
     * Creates the toolset, fails with {@link ClonerException} if a chosen mechanism is not available.
     *
     * @return toolset
     */
    public Toolset build() {
        Checks.illegalArg(reflection != null && moduleOpening != ModuleOpening.NONE,
            "Module opening applies to the default reflection only.");
        JdkPlatform platform = JdkPlatform.current();
        // reading sun.misc.Unsafe needs no module opening
        Lazy<UnsafeBridge> unsafe = new Lazy<>(() -> platform.unsafeBridge(platform.reflection(TypeOpener.NOOP)));
        Reflection reflection = this.reflection != null
            ? this.reflection
            : platform.reflection(platform.typeOpener(moduleOpening, unsafe));
        return new Toolset(reflection, buildAllocatorFactory(reflection, unsafe),
            buildFieldAdapterFactory(platform, reflection, unsafe));
    }

    private InstanceAllocatorFactory buildAllocatorFactory(Reflection reflection, Supplier<UnsafeBridge> unsafe) {
        List<InstanceAllocatorFactory> factories = new ArrayList<>();
        if (allocators != null) {
            factories.addAll(allocators);
        }
        else if (allocation != null) {
            allocation.forEach(variant -> factories.add(allocator(variant, unsafe)));
        }
        else if (ObjenesisUtils.isObjenesisAvailable()) {
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

    private static InstanceAllocatorFactory allocator(Allocation variant, Supplier<UnsafeBridge> unsafe) {
        switch (variant) {
            case OBJENESIS:
                if (!ObjenesisUtils.isObjenesisAvailable()) {
                    throw new ClonerException("Objenesis is not available: put it on the classpath.");
                }
                return new ObjenesisInstanceAllocatorFactory();
            case UNSAFE:
                return new UnsafeAllocatorFactory(unsafe.get());
            default:
                throw Checks.mustNotHappen();
        }
    }

    private FieldAdapterFactory buildFieldAdapterFactory(JdkPlatform platform, Reflection reflection,
        Supplier<UnsafeBridge> unsafe) {
        List<FieldAdapterFactory> factories = new ArrayList<>();
        if (fieldAdapters != null) {
            factories.addAll(fieldAdapters);
        }
        else if (fieldAccess != null) {
            fieldAccess.forEach(variant -> factories.add(fieldAdapters(variant, platform, reflection, unsafe)));
        }
        else {
            factories.add(ReflectionFieldAdapterImpl.factory(reflection));
            factories.add(unsafeIfAvailable(unsafe));
        }
        Checks.illegalArg(factories.isEmpty(), "No field access mechanisms.");
        return factories.size() == 1 ? factories.get(0) : new CompoundFieldAdapterFactory(factories);
    }

    private static FieldAdapterFactory fieldAdapters(FieldAccess variant, JdkPlatform platform, Reflection reflection,
        Supplier<UnsafeBridge> unsafe) {
        switch (variant) {
            case REFLECTION:
                return ReflectionFieldAdapterImpl.factory(reflection);
            case UNSAFE:
                return UnsafeFieldAdapterImpl.factory(unsafe.get());
            case VAR_HANDLE:
                return platform.varHandleFieldAdapters()
                    .orElseThrow(() -> new ClonerException("VarHandle field access needs JDK 9+."));
            case NARCISSUS:
                if (!NarcissusUtils.isNarcissusAvailable()) {
                    throw new ClonerException("Narcissus is not available: put it on the classpath.");
                }
                return NarcissusFieldAdapterImpl.FACTORY;
            default:
                throw Checks.mustNotHappen();
        }
    }

    /**
     * Unsafe field adapters created when first needed; if Unsafe is not available, the fields are left inaccessible.
     */
    private static FieldAdapterFactory unsafeIfAvailable(Supplier<UnsafeBridge> unsafe) {
        Lazy<Optional<FieldAdapterFactory>> factory = new Lazy<>(() -> {
            try {
                return Optional.of(UnsafeFieldAdapterImpl.factory(unsafe.get()));
            }
            catch (LinkageError | ClonerException e) {
                // e.g. a runtime image without jdk.unsupported
                return Optional.empty();
            }
        });
        return field -> factory.get().flatMap(adapters -> adapters.newAdapter(field));
    }

}
