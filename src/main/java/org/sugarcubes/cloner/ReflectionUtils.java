package org.sugarcubes.cloner;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Shortcuts of Java Reflection API.
 *
 * @author Maxim Butov
 */
public class ReflectionUtils {

    public static Field getField(Class<?> type, String name) {
        try {
            return makeAccessible(type.getDeclaredField(name));
        }
        catch (ReflectiveOperationException e) {
            throw new ClonerException(e);
        }
    }

    public static Method getMethod(Class<?> type, String name, Class<?> ... parameterTypes) {
        try {
            return makeAccessible(type.getDeclaredMethod(name, parameterTypes));
        }
        catch (ReflectiveOperationException e) {
            throw new ClonerException(e);
        }
    }

    public static <T extends AccessibleObject> T makeAccessible(T object) {
        object.setAccessible(true);
        return object;
    }

    private static final Field MODIFIERS = getField(Field.class, "modifiers");

    public static Field clearFinalModifier(Field field) {
        int modifiers = field.getModifiers();
        if (Modifier.isFinal(modifiers)) {
            try {
                MODIFIERS.set(field, modifiers ^ Modifier.FINAL);
            }
            catch (IllegalAccessException e) {
                throw new ClonerException(e);
            }
        }
        return field;
    }

}
