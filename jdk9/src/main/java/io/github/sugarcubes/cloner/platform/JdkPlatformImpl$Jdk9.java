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
package io.github.sugarcubes.cloner.platform;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.ModuleOpening;
import io.github.sugarcubes.cloner.reflection.ClonerAgent;
import io.github.sugarcubes.cloner.spi.FieldAdapterFactory;
import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.spi.ValuesModel;
import io.github.sugarcubes.cloner.unsafe.UnsafeBridge;
import io.github.sugarcubes.cloner.util.Checks;
import io.github.sugarcubes.cloner.util.NarcissusUtils;

/**
 * {@link JdkPlatform} of JDK 9+: modules, {@code jdk.internal.misc.Unsafe}, immutable collections of {@link List#of},
 * {@link Set#of} and {@link Map#of} rebuilt from their elements.
 *
 * @author Maxim Butov
 */
class JdkPlatformImpl$Jdk9 extends JdkPlatformImpl$Jdk8 {

    /**
     * Objects of the running JVM which appeared in JDK 9, with all their subtypes.
     */
    private static final List<Class<?>> RUNTIME_SUPER_TYPES = unmodifiableList(Module.class, ModuleLayer.class);

    /**
     * Models of immutable collections by their classes (the classes are not public).
     */
    private final Map<Class<?>, ValuesModel<?>> immutableCollections = immutableCollectionModels();

    private Map<Class<?>, ValuesModel<?>> immutableCollectionModels() {
        Map<Class<?>, ValuesModel<?>> models = new HashMap<>();
        // samples give the classes of the implementations
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();
        for (List<?> list : Arrays.asList(List.of(), List.of(a), List.of(a, b, c))) {
            models.put(list.getClass(), ImmutableCollectionModel.list(list.getClass(), this::immutableList));
        }
        for (Set<?> set : Arrays.asList(Set.of(), Set.of(a), Set.of(a, b, c))) {
            models.put(set.getClass(), ImmutableCollectionModel.set(set.getClass()));
        }
        for (Map<?, ?> map : Arrays.asList(Map.of(), Map.of(a, a), Map.of(a, a, b, b))) {
            models.put(map.getClass(), ImmutableCollectionModel.map(map.getClass()));
        }
        return Collections.unmodifiableMap(models);
    }

    @Override
    Kind kindOf(Class<?> type) {
        return isSubtype(type, RUNTIME_SUPER_TYPES) ? Kind.RUNTIME : super.kindOf(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<ValuesModel<T>> valuesModel(Class<T> type, Reflection reflection) {
        ValuesModel<T> model = (ValuesModel<T>) immutableCollections.get(type);
        return model != null ? Optional.of(model) : super.valuesModel(type, reflection);
    }

    /**
     * Creates an immutable list of the elements.
     *
     * @param values elements
     * @return immutable list
     */
    List<Object> immutableList(Object[] values) {
        return List.of(values);
    }

    @Override
    public Reflection reflection(ModuleOpening opening, Supplier<UnsafeBridge> unsafe) {
        return new ReflectionImpl$Jdk9(typeOpener(opening, unsafe));
    }

    private static TypeOpener typeOpener(ModuleOpening opening, Supplier<UnsafeBridge> unsafe) {
        switch (opening) {
            case NONE:
                return TypeOpener.NOOP;
            case UNSAFE:
                return new UnsafeModuleOpenerImpl(unsafe.get());
            case NARCISSUS:
                if (!NarcissusUtils.isNarcissusAvailable()) {
                    throw new ClonerException("Narcissus is not available: put it on the classpath.");
                }
                return new NarcissusModuleOpenerImpl();
            case INSTRUMENTATION:
                Instrumentation instrumentation = ClonerAgent.getInstrumentation();
                if (instrumentation == null) {
                    throw new ClonerException("Instrumentation is not available: start the JVM with -javaagent "
                        + "pointing to the cloner jar.");
                }
                return new InstrumentationModuleOpenerImpl(instrumentation);
            default:
                throw Checks.mustNotHappen();
        }
    }

    @Override
    public Optional<FieldAdapterFactory> varHandleFieldAdapters() {
        try {
            return Optional.of(VarHandleFieldAdapterImpl.FACTORY);
        }
        catch (LinkageError e) {
            throw new ClonerException("VarHandle field access needs the trusted lookup: open java.base/java.lang.invoke "
                + "to the cloner with --add-opens JVM option.", e);
        }
    }

    @Override
    public UnsafeBridge unsafeBridge(Reflection reflection) {
        try {
            return newJdkInternalMiscUnsafeBridge();
        }
        catch (LinkageError | ClonerException e) {
            // IllegalAccessError when jdk.internal.misc is not exported, fallback to sun.misc.Unsafe
            return super.unsafeBridge(reflection);
        }
    }

    /**
     * Creates the bridge to {@code jdk.internal.misc.Unsafe} of this JDK version.
     *
     * @return Unsafe bridge
     */
    UnsafeBridge newJdkInternalMiscUnsafeBridge() {
        return new JdkInternalMiscUnsafeBridge$Jdk9();
    }

}
