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
package io.github.sugarcubes.cloner.util;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Primitive types with their default values and wrapper classes.
 *
 * @author Maxim Butov
 */
public enum PrimitiveType {

    /**
     * {@code boolean}.
     */
    BOOLEAN(boolean.class),

    /**
     * {@code char}.
     */
    CHAR(char.class),

    /**
     * {@code byte}.
     */
    BYTE(byte.class),

    /**
     * {@code short}.
     */
    SHORT(short.class),

    /**
     * {@code int}.
     */
    INT(int.class),

    /**
     * {@code long}.
     */
    LONG(long.class),

    /**
     * {@code float}.
     */
    FLOAT(float.class),

    /**
     * {@code double}.
     */
    DOUBLE(double.class);

    /**
     * Constants by their primitive types.
     */
    private static final Map<Class<?>, PrimitiveType> BY_TYPE;

    static {
        PrimitiveType[] values = values();
        Map<Class<?>, PrimitiveType> byType = new HashMap<>(values.length, 1.0f);
        for (PrimitiveType primitive : values) {
            byType.put(primitive.type, primitive);
        }
        BY_TYPE = Collections.unmodifiableMap(byType);
    }

    private final Class<?> type;

    private final Object defaultValue;

    private final Class<?> wrapper;

    PrimitiveType(Class<?> type) {
        this.type = type;
        // the element of a new array has the default value of its type
        this.defaultValue = Array.get(Array.newInstance(type, 1), 0);
        this.wrapper = defaultValue.getClass();
    }

    /**
     * Returns the primitive type.
     *
     * @return primitive type, e.g. {@code int.class}
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Returns the default value.
     *
     * @return default value, boxed: {@code 0} or {@code false}
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the wrapper class.
     *
     * @return wrapper class, e.g. {@code Integer.class}
     */
    public Class<?> getWrapper() {
        return wrapper;
    }

    /**
     * Finds the constant of the type.
     *
     * @param type type
     * @return constant or {@code null} if the type is not primitive
     */
    public static PrimitiveType of(Class<?> type) {
        return BY_TYPE.get(type);
    }

    /**
     * Returns the default value of the type: {@code null}, {@code 0} or {@code false}.
     *
     * @param type type
     * @return default value, boxed for primitive types
     */
    public static Object defaultValue(Class<?> type) {
        PrimitiveType primitive = of(type);
        return primitive != null ? primitive.defaultValue : null;
    }

    /**
     * Returns the class of the values of the type: the wrapper class of a primitive type, the type itself otherwise.
     *
     * @param type type
     * @return class of the values
     */
    public static Class<?> wrapper(Class<?> type) {
        PrimitiveType primitive = of(type);
        return primitive != null ? primitive.wrapper : type;
    }

}
