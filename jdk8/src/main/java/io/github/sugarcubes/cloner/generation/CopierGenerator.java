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
package io.github.sugarcubes.cloner.generation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.CopyPolicy;
import io.github.sugarcubes.cloner.TwoPhaseObjectCopier;
import io.github.sugarcubes.cloner.platform.ClassDefiner;
import io.github.sugarcubes.cloner.platform.JdkPlatform;
import io.github.sugarcubes.cloner.spi.FieldAdapter;
import io.github.sugarcubes.cloner.spi.FieldsModel;
import io.github.sugarcubes.cloner.spi.InstanceAllocator;
import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.util.ClassUtils;
import io.github.sugarcubes.cloner.util.ClonerExceptionUtils;
import io.github.sugarcubes.cloner.util.Lazy;

/**
 * Generates copiers of classes copied by their fields: a class with the private access of the copied
 * class (an anonymous class of {@code sun.misc.Unsafe} on JDK 8-14, a hidden class in its nest on JDK 15+), written by
 * the ClassFile API on JDK 24+ or by ASM if it is on the classpath.
 *
 * @author Maxim Butov
 */
public final class CopierGenerator {

    private static final Lazy<Optional<CopierGenerator>> INSTANCE = new Lazy<>(CopierGenerator::create);

    /**
     * Constructors of the generated classes of a type by the signatures of their templates, {@link #FAILED} if the
     * class cannot be defined. Cloners share the classes: everything else a copier needs is passed to its
     * constructor. A constructor is referenced weakly here and strongly by the copiers, so a class nobody uses any
     * more is unloaded.
     */
    private static final ClassValue<ConcurrentMap<String, Object>> CONSTRUCTORS =
        new ClassValue<ConcurrentMap<String, Object>>() {
            @Override
            protected ConcurrentMap<String, Object> computeValue(Class<?> type) {
                return new ConcurrentHashMap<>();
            }
        };

    /**
     * Cached result of a class which cannot be defined.
     */
    private static final Object FAILED = new Object();

    /**
     * Reflection which makes fields accessible for their setters.
     */
    private static final Lazy<Reflection> REFLECTION = new Lazy<>(
        () -> JdkPlatform.current().lowLevelAccess().reflection());

    /**
     * Type of the constructor of the generated classes.
     */
    private static final MethodType CONSTRUCTOR = MethodType.methodType(void.class, InstanceAllocator.class,
        TwoPhaseObjectCopier.class, FieldAdapter[].class);

    private final ClassDefiner definer;

    private final BytecodeBackend backend;

    private CopierGenerator(ClassDefiner definer, BytecodeBackend backend) {
        this.definer = definer;
        this.backend = backend;
    }

    /**
     * Returns the generator if the JDK defines classes with the private access of a host and a backend is available.
     *
     * @return generator
     */
    public static Optional<CopierGenerator> instance() {
        return INSTANCE.get();
    }

    private static Optional<CopierGenerator> create() {
        ClassDefiner definer = JdkPlatform.current().classDefiner();
        if (!definer.isAvailable()) {
            return Optional.empty();
        }
        Optional<BytecodeBackend> backend = definer.classFileBackend();
        // ASM classes are checked by name: loading AsmBytecodeBackend without ASM fails
        if (!backend.isPresent() && ClassUtils.isClassAvailable("org.objectweb.asm.ClassWriter")) {
            backend = Optional.of(new AsmBytecodeBackend());
        }
        return backend.map(writer -> new CopierGenerator(definer, writer));
    }

    /**
     * Returns the bytecode backend.
     *
     * @return backend
     */
    public BytecodeBackend getBackend() {
        return backend;
    }

    /**
     * Generates the copier of the fields declared by the type.
     *
     * @param <T> type
     * @param model fields model of the type
     * @param policy field policy
     * @param parent copier of the super type or {@code null}
     * @return copier, or empty if the type has nothing to copy or the copier cannot be defined, e.g. the class loader
     *     of the type does not see the cloner
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<TwoPhaseObjectCopier<T>> generate(FieldsModel<T> model, CopyPolicy<Field> policy,
        TwoPhaseObjectCopier<? super T> parent) {
        CopierTemplate template = new CopierTemplate(model, policy, definer.supportsClassData() && finalsAccessible(model));
        if (template.isEmpty()) {
            return Optional.empty();
        }
        Class<T> type = model.getType();
        Optional<MethodHandle> constructor = constructor(type, template);
        if (!constructor.isPresent()) {
            return Optional.empty();
        }
        InstanceAllocator<T> allocator = model.getAllocator().orElseGet(() -> () -> {
            throw new ClonerException(String.format("No instance allocator supports %s.", type.getName()));
        });
        FieldAdapter[] adapters = template.adapters();
        GeneratedFieldsCopier<T> copier = (GeneratedFieldsCopier<T>) ClonerExceptionUtils.replaceException(
            () -> constructor.get().invoke(allocator, parent, adapters));
        copier.keep(constructor.get());
        return Optional.of(copier);
    }

    /**
     * Returns the constructor of the class of the template from the cache, defines the class if needed.
     *
     * @return constructor, or empty if the class cannot be defined
     */
    @SuppressWarnings("unchecked")
    private Optional<MethodHandle> constructor(Class<?> type, CopierTemplate template) {
        ConcurrentMap<String, Object> constructors = CONSTRUCTORS.get(type);
        while (true) {
            // a constructor defined now is held here until a copier holds it
            MethodHandle[] defined = new MethodHandle[1];
            Object cached = constructors.compute(template.signature(), (signature, current) -> {
                if (current == FAILED || current != null && ((WeakReference<?>) current).get() != null) {
                    return current;
                }
                defined[0] = define(type, template).orElse(null);
                return defined[0] != null ? new WeakReference<>(defined[0]) : FAILED;
            });
            if (cached == FAILED) {
                return Optional.empty();
            }
            MethodHandle constructor = defined[0] != null ? defined[0] : ((WeakReference<MethodHandle>) cached).get();
            if (constructor != null) {
                return Optional.of(constructor);
            }
            // collected right after the lookup: define the class again
        }
    }

    /**
     * Tells whether the class loader resolves the super class of the generated classes to the one of the cloner.
     */
    private static boolean seesCloner(ClassLoader loader) {
        try {
            return loader != null && Class.forName(GeneratedFieldsCopier.class.getName(), false, loader)
                == GeneratedFieldsCopier.class;
        }
        catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    /**
     * Tells whether the final fields declared by the type can be made accessible, so their setters can be created.
     */
    private static boolean finalsAccessible(FieldsModel<?> model) {
        Reflection reflection = REFLECTION.get();
        return Arrays.stream(model.getFields())
            .filter(field -> Modifier.isFinal(field.getModifiers()))
            .allMatch(field -> reflection.tryMakeAccessible(field).isPresent());
    }

    /**
     * Writes and defines the class of the template.
     *
     * @return constructor of the class, or empty if it cannot be defined
     */
    private Optional<MethodHandle> define(Class<?> type, CopierTemplate template) {
        if (!seesCloner(type.getClassLoader())) {
            // e.g. JDK classes: the generated class could not extend GeneratedFieldsCopier
            return Optional.empty();
        }
        try {
            List<MethodHandle> setters = new ArrayList<>();
            for (Field field : template.setterFields()) {
                setters.add(MethodHandles.lookup().unreflectSetter(field));
            }
            byte[] bytes = template.write(backend, definer.classVersion());
            return Optional.of(definer.defineNestmate(type, bytes, GeneratedFieldsCopier.class, CONSTRUCTOR,
                setters.isEmpty() ? null : Collections.unmodifiableList(setters)));
        }
        catch (ClonerException | LinkageError | IllegalAccessException e) {
            return Optional.empty();
        }
    }

}
