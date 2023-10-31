package io.github.sugarcubes.cloner.internal;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface Reflection {

    /**
     * Returns accessible declared fields of the class.
     * Same as {@link Class#getDeclaredFields()}.
     *
     * @param type {@link Class} instance
     * @return declared fields
     */
    Field[] getDeclaredFields(Class<?> type);

    /**
     * Returns accessible field.
     * Same as {@link Class#getDeclaredField(String)}.
     *
     * @param type {@link Class} instance
     * @param name field name
     * @return field
     */
    Field getDeclaredField(Class<?> type, String name);

    /**
     * Returns accessible method.
     * Same as {@link Class#getDeclaredMethod(String, Class[])}.
     *
     * @param type {@link Class} instance
     * @param name method name
     * @param parameterTypes parameter types
     * @return method
     */
    Method getDeclaredMethod(Class<?> type, String name, Class<?>... parameterTypes);

    /**
     * Returns accessible constructor.
     * Same as {@link Class#getDeclaredConstructor(Class[])}.
     *
     * @param type {@link Class} instance
     * @param parameterTypes parameter types
     * @param <T> type
     * @return constructor
     */
    <T> Constructor<T> getDeclaredConstructor(Class<T> type, Class<?>... parameterTypes);

    /**
     * Makes object accessible.
     *
     * @param <T> can be field or method or constructor
     * @param member field or method or constructor
     * @return same object
     */
    <T extends AccessibleObject> T makeAccessible(T member);

}
