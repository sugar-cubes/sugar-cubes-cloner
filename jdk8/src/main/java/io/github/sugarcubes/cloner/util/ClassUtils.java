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

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static io.github.sugarcubes.cloner.util.ClonerExceptionUtils.replaceException;

/**
 * Class-related utilities.
 *
 * @author Maxim Butov
 */
public class ClassUtils {

    /**
     * Checks the availability of the class in current class loader.
     *
     * @param className name of the class
     * @return {@code true} if class is available
     */
    public static boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks whether an instance of the type can be allocated without arguments, as instance allocators do.
     * Interfaces, abstract classes and primitives have no own instances. Arrays have, but an array is created
     * with its length, so arrays are copied by separate copiers and never reach instance allocators.
     *
     * @param type class
     * @return {@code true} if an instance of the type can be allocated without arguments
     */
    public static boolean isAllocatable(Class<?> type) {
        return !type.isArray() && !type.isPrimitive() && !Modifier.isAbstract(type.getModifiers());
    }

    /**
     * Checks whether the class declares a constructor without parameters.
     *
     * @param type class
     * @return {@code true} if there is a constructor without parameters
     */
    public static boolean hasNoArgConstructor(Class<?> type) {
        return Arrays.stream(type.getDeclaredConstructors()).anyMatch(constructor -> constructor.getParameterCount() == 0);
    }

    /**
     * Version of {@link Class#forName(String)} with unchecked exception.
     *
     * @param className class name
     * @param <T> type
     * @return {@link Class} instance
     */
    public static <T> Class<T> classForName(String className) {
        return (Class<T>) replaceException(() -> Class.forName(className));
    }

    /**
     * Utility class.
     */
    private ClassUtils() {
    }

}
