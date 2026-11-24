/*
 * Copyright 2017-2026 the original author or authors.
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
package io.github.sugarcubes.cloner.engine;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import io.github.sugarcubes.cloner.CopyContext;
import io.github.sugarcubes.cloner.ObjectCopier;
import io.github.sugarcubes.cloner.platform.JdkPlatform;
import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.util.Lazy;

/**
 * One-phase copier which creates a shallow clone of the original object: arrays are copied element by element, other
 * objects are cloned with their public {@code clone()} method. Only a class without a public {@code clone()} needs
 * {@link Object#clone()} made accessible, which needs {@code java.lang} opened on JDK 9+.
 *
 * @author Maxim Butov
 */
public class CloneableCopier<T extends Cloneable> implements ObjectCopier<T> {

    private static final String CLONE = "clone";

    /**
     * Public {@code clone()} method of a class, declared by a public class of its hierarchy, so it can be invoked
     * without access checks; empty if there is no such method.
     */
    private static final ClassValue<Optional<Method>> PUBLIC_CLONE = new ClassValue<Optional<Method>>() {
        @Override
        protected Optional<Method> computeValue(Class<?> type) {
            return findPublicClone(type);
        }
    };

    /**
     * {@link Object#clone()} made accessible when first needed.
     */
    private static final Lazy<Method> OBJECT_CLONE = new Lazy<>(() -> {
        Reflection reflection = JdkPlatform.current().lowLevelAccess().reflection();
        return reflection.makeAccessible(reflection.getDeclaredMethod(Object.class, CLONE));
    });

    @Override
    @SuppressWarnings("unchecked")
    public T copy(T original, CopyContext context) throws Exception {
        Class<?> type = original.getClass();
        Object clone = type.isArray()
            ? cloneArray(original)
            : PUBLIC_CLONE.get(type).orElseGet(OBJECT_CLONE).invoke(original);
        return context.register((T) clone);
    }

    private static Object cloneArray(Object array) {
        int length = Array.getLength(array);
        Object clone = Array.newInstance(array.getClass().getComponentType(), length);
        System.arraycopy(array, 0, clone, 0, length);
        return clone;
    }

    private static Optional<Method> findPublicClone(Class<?> type) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            if (Modifier.isPublic(current.getModifiers())) {
                try {
                    Method clone = current.getMethod(CLONE);
                    if (Modifier.isPublic(clone.getDeclaringClass().getModifiers())) {
                        return Optional.of(clone);
                    }
                }
                catch (NoSuchMethodException e) {
                    // Object.clone() is protected, the class does not make it public
                }
            }
        }
        return Optional.empty();
    }

}
