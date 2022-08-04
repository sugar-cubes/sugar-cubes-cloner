package org.sugarcubes.cloner;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.sugarcubes.cloner.Executable.unchecked;

/**
 * Shortcuts for Java Reflection API.
 *
 * @author Maxim Butov
 */
public class ReflectionUtils {

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
     * Version of {@link Class#forName(String)} with unchecked exception.
     *
     * @param className class name
     * @return {@link Class} instance
     */
    public static Class<?> classForName(String className) {
        return unchecked(() -> Class.forName(className));
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
        return unchecked(() -> makeAccessible(type.getDeclaredField(name)));
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
        return unchecked(() -> makeAccessible(type.getDeclaredMethod(name, parameterTypes)));
    }

    /**
     * Makes object accessible.
     *
     * @param <T> can be field or method or constructor
     * @param object field or method or constructor
     * @return same object
     */
    public static <T extends AccessibleObject> T makeAccessible(T object) {
        object.setAccessible(true);
        return object;
    }

    /**
     * Utility class.
     */
    private ReflectionUtils() {
    }

}
