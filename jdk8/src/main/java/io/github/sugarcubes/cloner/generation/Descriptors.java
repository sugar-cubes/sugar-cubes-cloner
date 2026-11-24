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

import java.util.HashMap;
import java.util.Map;

/**
 * Internal names and descriptors of classes.
 *
 * @author Maxim Butov
 */
final class Descriptors {

    private static final Map<Class<?>, String> PRIMITIVES = new HashMap<>();

    static {
        PRIMITIVES.put(boolean.class, "Z");
        PRIMITIVES.put(byte.class, "B");
        PRIMITIVES.put(char.class, "C");
        PRIMITIVES.put(short.class, "S");
        PRIMITIVES.put(int.class, "I");
        PRIMITIVES.put(long.class, "J");
        PRIMITIVES.put(float.class, "F");
        PRIMITIVES.put(double.class, "D");
        PRIMITIVES.put(void.class, "V");
    }

    private Descriptors() {
    }

    /**
     * Returns the internal name of a class, e.g. {@code java/lang/Object}.
     *
     * @param type class
     * @return internal name
     */
    static String internalName(Class<?> type) {
        return type.getName().replace('.', '/');
    }

    /**
     * Returns the descriptor of a type, e.g. {@code I} or {@code Ljava/lang/Object;}.
     *
     * @param type type
     * @return descriptor
     */
    static String descriptor(Class<?> type) {
        if (type.isPrimitive()) {
            return PRIMITIVES.get(type);
        }
        return type.isArray() ? internalName(type) : "L" + internalName(type) + ";";
    }

    /**
     * Returns the operand of {@code checkcast} for a reference type: internal name of a class, descriptor of an array.
     *
     * @param type reference type
     * @return operand
     */
    static String castType(Class<?> type) {
        return internalName(type);
    }

    /**
     * Returns the descriptor of a method.
     *
     * @param returnType return type
     * @param parameterTypes parameter types
     * @return descriptor
     */
    static String method(Class<?> returnType, Class<?>... parameterTypes) {
        StringBuilder descriptor = new StringBuilder("(");
        for (Class<?> parameterType : parameterTypes) {
            descriptor.append(descriptor(parameterType));
        }
        return descriptor.append(')').append(descriptor(returnType)).toString();
    }

}
