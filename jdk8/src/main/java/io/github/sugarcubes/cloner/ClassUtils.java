/*
 * Copyright 2017-2023 the original author or authors.
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

import static io.github.sugarcubes.cloner.ClonerExceptionUtils.replaceException;

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
     * Checks the availability of the method in the class.
     *
     * @param type class
     * @param methodName name of the method
     * @param parameterTypes parameter types
     * @return {@code true} if the method is available
     */
    public static boolean isMethodAvailable(Class<?> type, String methodName, Class<?>... parameterTypes) {
        try {
            type.getDeclaredMethod(methodName, parameterTypes);
            return true;
        }
        catch (NoSuchMethodException e) {
            return false;
        }
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
