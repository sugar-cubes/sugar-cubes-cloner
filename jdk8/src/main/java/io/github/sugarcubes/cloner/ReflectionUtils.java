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
package io.github.sugarcubes.cloner;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static io.github.sugarcubes.cloner.ClonerExceptionUtils.replaceException;

/**
 * Shortcuts for Java Reflection API.
 *
 * @author Maxim Butov
 */
public class ReflectionUtils {

    /**
     * Returns accessible declared fields of the class.
     * Same as {@link Class#getDeclaredFields()}.
     *
     * @param type {@link Class} instance
     * @return declared fields
     */
    public static Field[] getDeclaredFields(Class<?> type) {
        return makeAccessible(type).getDeclaredFields();
    }

    /**
     * Returns accessible field.
     * Same as {@link Class#getDeclaredField(String)}.
     *
     * @param type {@link Class} instance
     * @param name field name
     * @return field
     */
    public static Field getField(Class<?> type, String name) {
        return replaceException(() -> makeAccessible(makeAccessible(type).getDeclaredField(name)));
    }

    /**
     * Returns accessible method.
     * Same as {@link Class#getDeclaredMethod(String, Class[])}.
     *
     * @param type {@link Class} instance
     * @param name method name
     * @param parameterTypes parameter types
     * @return method
     */
    public static Method getMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        return replaceException(() -> makeAccessible(makeAccessible(type).getDeclaredMethod(name, parameterTypes)));
    }

    /**
     * Returns accessible constructor.
     * Same as {@link Class#getDeclaredConstructor(Class[])}.
     *
     * @param type {@link Class} instance
     * @param parameterTypes parameter types
     * @param <T> type
     * @return constructor
     */
    public static <T> Constructor<T> getConstructor(Class<T> type, Class<?>... parameterTypes) {
        return replaceException(() -> makeAccessible(makeAccessible(type).getDeclaredConstructor(parameterTypes)));
    }

    /**
     * Makes class accessible from cloner module.
     *
     * @param <T> type
     * @param type object type
     * @return same type
     */
    public static <T> Class<T> makeAccessible(Class<T> type) {
        JdkConfigurationHolder.CONFIGURATION.makeAccessible(type);
        return type;
    }

    /**
     * Makes object accessible.
     *
     * @param <T> can be field or method or constructor
     * @param object field or method or constructor
     * @return same object
     */
    public static <T extends AccessibleObject> T makeAccessible(T object) {
        try {
            object.setAccessible(true);
        }
        catch (Exception e) {
            throw new ClonerException(
                "Error making a member accessible. " +
                    "On JDK 9..16 you can use --illegal-access=permit java parameter. " +
                    "On JDK 17+ you need to configure modules opens.",
                e);
        }
        return object;
    }

    /**
     * Returns {@code true} if the member of class is non-static.
     *
     * @param member field or method or constructor
     * @return {@code true} if the member is non-static
     */
    public static boolean isNonStatic(Member member) {
        return !Modifier.isStatic(member.getModifiers());
    }

    /**
     * Creates new instance of type using no-arg constructor.
     *
     * @param <T> type
     * @param type object type
     * @return new instance
     */
    public static <T> T newInstance(Class<T> type) {
        Constructor<T> constructor = getConstructor(type);
        return replaceException(constructor::newInstance);
    }

    /**
     * Creates new instance of type using no-arg constructor.
     *
     * @param <T> type
     * @param className class name
     * @return new instance
     */
    public static <T> T newInstance(String className) {
        return newInstance(ClassUtils.classForName(className));
    }

    /**
     * Utility class.
     */
    private ReflectionUtils() {
    }

}
