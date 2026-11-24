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
package io.github.sugarcubes.cloner.generation;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.CopyContext;
import io.github.sugarcubes.cloner.engine.RegistrationContext;

/**
 * Contract of copy constructors, which the build tools of the cloner add to classes: the type of the marker
 * parameter and the methods called by the generated code.
 *
 * <p>A copy constructor of class {@code C} is a synthetic constructor
 * {@code C(CopyConstructors marker, C original, CopyContext context)}, the marker is always {@code null}. It is private
 * in final classes and protected in the others, so constructors of subclasses can call it. It calls the copy
 * constructor of the superclass, or the constructor without arguments of {@code Object} or {@code Record} and then
 * {@link #register(CopyContext, Object)}; then copies the fields declared by {@code C}: primitive values as they are,
 * references by {@link #copy(CopyContext, Object, Object, Class, String)}.</p>
 *
 * <p>The constructor writes final fields directly and creates the clone without {@code ReflectionFactory} or
 * {@code Unsafe}. The cloner uses it when the field policy copies all fields of the class by default.</p>
 *
 * @author Maxim Butov
 */
public final class CopyConstructors {

    /**
     * Internal name of this class, for the generated code.
     */
    public static final String INTERNAL_NAME = "io/github/sugarcubes/cloner/generation/CopyConstructors";

    private CopyConstructors() {
    }

    /**
     * Returns the descriptor of the copy constructor of the class.
     *
     * @param internalName internal name of the class
     * @return constructor descriptor
     */
    public static String descriptor(String internalName) {
        return String.format("(L%s;L%s;Lio/github/sugarcubes/cloner/CopyContext;)V", INTERNAL_NAME, internalName);
    }

    /**
     * Finds the copy constructor declared by the class.
     *
     * @param <T> type
     * @param type class
     * @return constructor or empty if the class has none
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<Constructor<T>> find(Class<T> type) {
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            if (Arrays.equals(constructor.getParameterTypes(), new Class<?>[] {CopyConstructors.class, type,
                CopyContext.class})) {
                return Optional.of((Constructor<T>) constructor);
            }
        }
        return Optional.empty();
    }

    /**
     * Registers the clone before its fields are copied, so reference cycles get it. Called by the copy constructor of
     * the topmost class.
     *
     * @param context copy context
     * @param clone clone being constructed
     */
    public static void register(CopyContext context, Object clone) {
        context.register(clone);
    }

    /**
     * Copies the value of a reference field. If the clone of the value is being created right now (a reference
     * cycle), returns {@code null} and writes the field when the clone is ready.
     *
     * @param context copy context
     * @param value original value of the field
     * @param target clone being constructed
     * @param declaringClass class declaring the field
     * @param field name of the field
     * @return clone of the value or {@code null}
     * @throws Exception if copying fails
     */
    public static Object copy(CopyContext context, Object value, Object target, Class<?> declaringClass, String field)
        throws Exception {
        if (value == null) {
            return null;
        }
        Object clone = context.copyNow(value);
        if (clone != CopyContext.PENDING) {
            return clone;
        }
        if (!(context instanceof RegistrationContext)) {
            throw new ClonerException(String.format("Copy constructor of %s called outside of the cloner.",
                declaringClass.getName()));
        }
        ((RegistrationContext) context).subscribeField(value, target, declaringClass, field);
        return null;
    }

}
