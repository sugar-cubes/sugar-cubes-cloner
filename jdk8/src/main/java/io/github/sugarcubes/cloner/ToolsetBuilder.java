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
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
import io.github.sugarcubes.cloner.spi.InstanceAllocator;
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

    /**
     * What mechanisms are created from.
     */
    private static final class Context {

        final JdkPlatform platform;

        final Reflection reflection;

        final Supplier<UnsafeBridge> unsafe;

        Context(JdkPlatform platform, Reflection reflection, Supplier<UnsafeBridge> unsafe) {
            this.platform = platform;
            this.reflection = reflection;
            this.unsafe = unsafe;
        }

    }

    /**
     * Mechanism to create, with its description.
     */
    private static final class Mechanism<T> {

        final String description;

        final Function<Context, T> factory;

        Mechanism(String description, Function<Context, T> factory) {
            this.description = description;
            this.factory = factory;
        }

    }

    private Reflection reflection;
    private ModuleOpening moduleOpening = ModuleOpening.NONE;
    private List<Mechanism<InstanceAllocatorFactory>> allocators;
    private InstanceAllocatorFactory constructorFallback;
    private boolean constructorFallbackDisabled;
    private List<Mechanism<FieldAdapterFactory>> fieldAdapters;

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
     * Adds mechanisms creating instances without constructors. Mechanisms and custom allocators are tried in the
     * order of adding; for every type the first one supporting it is used.
     *
     * @param allocation mechanisms in order of preference
     * @return same builder instance
     */
    public ToolsetBuilder allocation(Allocation... allocation) {
        List<Mechanism<InstanceAllocatorFactory>> list = allocatorList();
        for (Allocation variant : allocation) {
            Checks.argNotNull(variant, "Allocation");
            list.add(new Mechanism<>(variant.name(), context -> allocator(variant, context.unsafe)));
        }
        return this;
    }

    /**
     * Adds a custom instance allocator factory, which must not call constructors.
     *
     * @param allocator instance allocator factory
     * @return same builder instance
     */
    public ToolsetBuilder allocator(InstanceAllocatorFactory allocator) {
        return allocators(allocator);
    }

    /**
     * Adds custom instance allocator factories, which must not call constructors. Mechanisms and custom allocators
     * are tried in the order of adding; for every type the first one supporting it is used.
     *
     * @param allocators instance allocator factories in order of preference
     * @return same builder instance
     */
    public ToolsetBuilder allocators(InstanceAllocatorFactory... allocators) {
        List<Mechanism<InstanceAllocatorFactory>> list = allocatorList();
        for (InstanceAllocatorFactory allocator : allocators) {
            Checks.argNotNull(allocator, "Instance allocator factory");
            Checks.illegalArg(allocator.invokesConstructor(),
                "%s calls constructors, set it as constructor fallback.", allocator);
            list.add(new Mechanism<>(String.valueOf(allocator), context -> allocator));
        }
        return this;
    }

    private List<Mechanism<InstanceAllocatorFactory>> allocatorList() {
        if (allocators == null) {
            allocators = new ArrayList<>();
        }
        return allocators;
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
     * Adds mechanisms reading and writing fields. Mechanisms and custom adapters are tried in the order of adding; for
     * every field the first one able to access it is used.
     *
     * @param fieldAccess mechanisms in order of preference
     * @return same builder instance
     */
    public ToolsetBuilder fieldAccess(FieldAccess... fieldAccess) {
        List<Mechanism<FieldAdapterFactory>> list = fieldAdapterList();
        for (FieldAccess variant : fieldAccess) {
            Checks.argNotNull(variant, "Field access");
            list.add(new Mechanism<>(variant.name(), context -> fieldAdapters(variant, context)));
        }
        return this;
    }

    /**
     * Adds a custom field adapter factory.
     *
     * @param fieldAdapter field adapter factory
     * @return same builder instance
     */
    public ToolsetBuilder fieldAdapter(FieldAdapterFactory fieldAdapter) {
        return fieldAdapters(fieldAdapter);
    }

    /**
     * Adds custom field adapter factories. Mechanisms and custom adapters are tried in the order of adding; for every
     * field the first one able to access it is used.
     *
     * @param fieldAdapters field adapter factories in order of preference
     * @return same builder instance
     */
    public ToolsetBuilder fieldAdapters(FieldAdapterFactory... fieldAdapters) {
        List<Mechanism<FieldAdapterFactory>> list = fieldAdapterList();
        for (FieldAdapterFactory fieldAdapter : fieldAdapters) {
            Checks.argNotNull(fieldAdapter, "Field adapter factory");
            list.add(new Mechanism<>(String.valueOf(fieldAdapter), context -> fieldAdapter));
        }
        return this;
    }

    private List<Mechanism<FieldAdapterFactory>> fieldAdapterList() {
        if (fieldAdapters == null) {
            fieldAdapters = new ArrayList<>();
        }
        return fieldAdapters;
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
        Lazy<UnsafeBridge> unsafe = new Lazy<>(() -> unsafeBridge(platform));
        Reflection reflection = this.reflection != null
            ? this.reflection
            : platform.reflection(platform.typeOpener(moduleOpening, unsafe));
        Context context = new Context(platform, reflection, unsafe);

        List<Mechanism<InstanceAllocatorFactory>> allocators = this.allocators != null
            ? this.allocators
            : defaultAllocators();
        List<Mechanism<FieldAdapterFactory>> fieldAdapters = this.fieldAdapters != null
            ? this.fieldAdapters
            : defaultFieldAdapters();
        Object opening = this.reflection != null ? "custom reflection" : moduleOpening;
        Object fallback = constructorFallbackDisabled ? "none"
            : constructorFallback != null ? constructorFallback : "constructors";
        String description = String.format("Toolset[moduleOpening=%s, allocation=%s, constructorFallback=%s, %s]",
            opening, describe(allocators), fallback, "fieldAccess=" + describe(fieldAdapters));
        return new Toolset(reflection, buildAllocatorFactory(allocators, context),
            buildFieldAdapterFactory(fieldAdapters, context), description);
    }

    private static UnsafeBridge unsafeBridge(JdkPlatform platform) {
        try {
            // reading sun.misc.Unsafe needs no module opening
            return platform.unsafeBridge(platform.reflection(TypeOpener.NOOP));
        }
        catch (LinkageError e) {
            throw new ClonerException("Unsafe is not available.", e);
        }
    }

    private static <T> String describe(List<Mechanism<T>> mechanisms) {
        return mechanisms.stream().map(mechanism -> mechanism.description).collect(Collectors.joining(", ", "[", "]"));
    }

    private static List<Mechanism<InstanceAllocatorFactory>> defaultAllocators() {
        List<Mechanism<InstanceAllocatorFactory>> list = new ArrayList<>();
        if (ObjenesisUtils.isObjenesisAvailable()) {
            list.add(new Mechanism<>(Allocation.OBJENESIS.name(), context -> new ObjenesisInstanceAllocatorFactory()));
        }
        else {
            list.add(new Mechanism<>(Allocation.UNSAFE.name() + " if available",
                context -> unsafeAllocatorIfAvailable(context.unsafe)));
        }
        return list;
    }

    private static List<Mechanism<FieldAdapterFactory>> defaultFieldAdapters() {
        List<Mechanism<FieldAdapterFactory>> list = new ArrayList<>();
        list.add(new Mechanism<>(FieldAccess.REFLECTION.name(),
            context -> ReflectionFieldAdapterImpl.factory(context.reflection)));
        list.add(new Mechanism<>(FieldAccess.UNSAFE.name() + " if reflection cannot access",
            context -> unsafeIfAvailable(context.unsafe)));
        return list;
    }

    private InstanceAllocatorFactory buildAllocatorFactory(List<Mechanism<InstanceAllocatorFactory>> mechanisms,
        Context context) {
        List<InstanceAllocatorFactory> factories = new ArrayList<>();
        mechanisms.forEach(mechanism -> factories.add(mechanism.factory.apply(context)));
        if (!constructorFallbackDisabled) {
            factories.add(constructorFallback != null
                ? constructorFallback
                : new ReflectionInstanceAllocatorFactory(context.reflection));
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

    private static FieldAdapterFactory buildFieldAdapterFactory(List<Mechanism<FieldAdapterFactory>> mechanisms,
        Context context) {
        Checks.illegalArg(mechanisms.isEmpty(), "No field access mechanisms.");
        List<FieldAdapterFactory> factories = new ArrayList<>();
        mechanisms.forEach(mechanism -> factories.add(mechanism.factory.apply(context)));
        return factories.size() == 1 ? factories.get(0) : new CompoundFieldAdapterFactory(factories);
    }

    private static FieldAdapterFactory fieldAdapters(FieldAccess variant, Context context) {
        switch (variant) {
            case REFLECTION:
                return ReflectionFieldAdapterImpl.factory(context.reflection);
            case UNSAFE:
                return UnsafeFieldAdapterImpl.factory(context.unsafe.get());
            case VAR_HANDLE:
                return context.platform.varHandleFieldAdapters()
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
     * Unsafe allocator created when first needed; if Unsafe is not available, the types are left to the constructor
     * fallback.
     */
    private static InstanceAllocatorFactory unsafeAllocatorIfAvailable(Supplier<UnsafeBridge> unsafe) {
        Lazy<Optional<InstanceAllocatorFactory>> factory = new Lazy<>(() -> {
            try {
                return Optional.of(new UnsafeAllocatorFactory(unsafe.get()));
            }
            catch (ClonerException e) {
                // e.g. a runtime image without jdk.unsupported
                return Optional.empty();
            }
        });
        return new InstanceAllocatorFactory() {
            @Override
            public <T> Optional<InstanceAllocator<T>> newAllocator(Class<T> type) {
                return factory.get().flatMap(allocators -> allocators.newAllocator(type));
            }
        };
    }

    /**
     * Unsafe field adapters created when first needed; if Unsafe is not available, the fields are left inaccessible.
     */
    private static FieldAdapterFactory unsafeIfAvailable(Supplier<UnsafeBridge> unsafe) {
        Lazy<Optional<FieldAdapterFactory>> factory = new Lazy<>(() -> {
            try {
                return Optional.of(UnsafeFieldAdapterImpl.factory(unsafe.get()));
            }
            catch (ClonerException e) {
                // e.g. a runtime image without jdk.unsupported
                return Optional.empty();
            }
        });
        return field -> factory.get().flatMap(adapters -> adapters.newAdapter(field));
    }

}
