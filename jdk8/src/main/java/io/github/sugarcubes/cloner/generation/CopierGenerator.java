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
import java.lang.reflect.Field;
import java.util.Optional;

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.CopyPolicy;
import io.github.sugarcubes.cloner.TwoPhaseObjectCopier;
import io.github.sugarcubes.cloner.platform.JdkPlatform;
import io.github.sugarcubes.cloner.spi.FieldAdapter;
import io.github.sugarcubes.cloner.spi.FieldsModel;
import io.github.sugarcubes.cloner.spi.InstanceAllocator;
import io.github.sugarcubes.cloner.util.ClonerExceptionUtils;
import io.github.sugarcubes.cloner.util.Lazy;

/**
 * Generates copiers of classes listed by {@link GenerationTargets}: a hidden class in the nest of the copied class,
 * JDK 15+, written by the ClassFile API on JDK 24+ or by ASM if it is on the classpath.
 *
 * @author Maxim Butov
 */
public final class CopierGenerator {

    private static final Lazy<Optional<CopierGenerator>> INSTANCE = new Lazy<>(CopierGenerator::create);

    /**
     * Type of the constructor of the generated classes.
     */
    private static final MethodType CONSTRUCTOR = MethodType.methodType(void.class, InstanceAllocator.class,
        TwoPhaseObjectCopier.class, FieldAdapter[].class);

    private final JdkPlatform platform;

    private final BytecodeBackend backend;

    private CopierGenerator(JdkPlatform platform, BytecodeBackend backend) {
        this.platform = platform;
        this.backend = backend;
    }

    /**
     * Returns the generator if the JDK defines hidden classes and a bytecode backend is available.
     *
     * @return generator
     */
    public static Optional<CopierGenerator> instance() {
        return INSTANCE.get();
    }

    private static Optional<CopierGenerator> create() {
        JdkPlatform platform = JdkPlatform.current();
        if (!platform.supportsHiddenClasses()) {
            return Optional.empty();
        }
        Optional<BytecodeBackend> backend = platform.classFileBackend();
        if (!backend.isPresent() && AsmBytecodeBackend.isAvailable()) {
            backend = Optional.of(new AsmBytecodeBackend());
        }
        return backend.map(writer -> new CopierGenerator(platform, writer));
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
        CopierTemplate template = new CopierTemplate(model, policy);
        if (template.isEmpty()) {
            return Optional.empty();
        }
        Class<T> type = model.getType();
        InstanceAllocator<T> allocator = model.getAllocator().orElseGet(() -> () -> {
            throw new ClonerException(String.format("No instance allocator supports %s.", type.getName()));
        });
        try {
            MethodHandles.Lookup lookup = platform.defineNestmate(type, template.write(backend),
                GeneratedFieldsCopier.class);
            MethodHandle constructor = lookup.findConstructor(lookup.lookupClass(), CONSTRUCTOR);
            FieldAdapter[] adapters = template.adapters();
            return Optional.of((TwoPhaseObjectCopier<T>) ClonerExceptionUtils.replaceException(
                () -> constructor.invoke(allocator, parent, adapters)));
        }
        catch (ClonerException | LinkageError | ReflectiveOperationException e) {
            return Optional.empty();
        }
    }

}
