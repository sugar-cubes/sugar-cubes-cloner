package org.sugarcubes.cloner;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Shortcuts for Java Reflection API.
 *
 * @author Maxim Butov
 */
public class ReflectionUtils {

    /**
     * Variant of the {@link java.util.function.Supplier} with declared exception.
     */
    @FunctionalInterface
    public interface ReflectionAction<T> {

        /**
         * Actual work.
         */
        T get() throws ReflectiveOperationException;

    }

    /**
     * Void modification of {@link ReflectionAction}.
     */
    @FunctionalInterface
    public interface VoidReflectionAction extends ReflectionAction<Object> {

        @Override
        default Object get() throws ReflectiveOperationException {
            run();
            return null;
        }

        /**
         * Actual work.
         */
        void run() throws ReflectiveOperationException;

    }

    /**
     * Executes call to Reflection API and replaces {@link ReflectiveOperationException} with {@link ClonerException}.
     *
     * @param <T> object type
     * @param action code to execute
     * @return execution result
     */
    public static <T> T execute(ReflectionAction<T> action) {
        try {
            return action.get();
        }
        catch (ReflectiveOperationException e) {
            throw new ClonerException(e);
        }
    }

    /**
     * Checks the availability of the class in current class loader.
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
        return execute(() -> Class.forName(className));
    }

    /**
     * Returns accessible field.
     */
    public static Field getField(Class<?> type, String name) {
        return execute(() -> makeAccessible(type.getDeclaredField(name)));
    }

    /**
     * Returns accessible method.
     */
    public static Method getMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        return execute(() -> makeAccessible(type.getDeclaredMethod(name, parameterTypes)));
    }

    /**
     * Makes object accessible.
     *
     * @param <T> can be field or method or constructor
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
