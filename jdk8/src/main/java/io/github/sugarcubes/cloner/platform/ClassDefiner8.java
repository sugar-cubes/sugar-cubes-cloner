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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.generation.BytecodeBackend;
import io.github.sugarcubes.cloner.reflection.ReflectionImpl8;
import io.github.sugarcubes.cloner.unsafe.SunMiscUnsafeBridge;
import io.github.sugarcubes.cloner.util.ClonerExceptionUtils;
import io.github.sugarcubes.cloner.util.Lazy;

/**
 * {@link ClassDefiner} of JDK 8-14: anonymous classes of {@code sun.misc.Unsafe}.
 *
 * @author Maxim Butov
 */
class ClassDefiner8 implements ClassDefiner {

    /**
     * Version of anonymous classes: Java 5, verified by type inference without stack map frames. Frames would name
     * the class itself, and an anonymous class cannot be loaded by name.
     */
    private static final int ANONYMOUS_CLASS_VERSION = 49;

    /**
     * {@code sun.misc.Unsafe.defineAnonymousClass(Class, byte[], Object[])}, removed in JDK 17.
     */
    private static final Lazy<Optional<Method>> DEFINE_ANONYMOUS_CLASS = new Lazy<>(() -> {
        try {
            return Optional.of(sun.misc.Unsafe.class.getMethod("defineAnonymousClass", Class.class, byte[].class,
                Object[].class));
        }
        catch (NoSuchMethodException | LinkageError e) {
            return Optional.empty();
        }
    });

    @Override
    public boolean isAvailable() {
        return DEFINE_ANONYMOUS_CLASS.get().isPresent();
    }

    @Override
    public int classVersion() {
        return ANONYMOUS_CLASS_VERSION;
    }

    /**
     * Defines an anonymous class of {@code sun.misc.Unsafe}, which has the access of its host class; JDK 8-16.
     */
    @Override
    public MethodHandle defineNestmate(Class<?> host, byte[] bytes, Class<?> support, MethodType constructor) {
        Method define = DEFINE_ANONYMOUS_CLASS.get()
            .orElseThrow(() -> new ClonerException("Unsafe.defineAnonymousClass() is not available."));
        return ClonerExceptionUtils.replaceException(() -> {
            Object unsafe = new SunMiscUnsafeBridge(new ReflectionImpl8()).getUnsafe();
            Class<?> anonymous = (Class<?>) define.invoke(unsafe, host, bytes, null);
            // not public: its constructor is made accessible
            Constructor<?> declared = anonymous.getDeclaredConstructor(constructor.parameterArray());
            declared.setAccessible(true);
            return MethodHandles.lookup().unreflectConstructor(declared);
        });
    }

    @Override
    public Optional<BytecodeBackend> classFileBackend() {
        return Optional.empty();
    }

}
