package io.github.sugarcubes.cloner.internal;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import static io.github.sugarcubes.cloner.ClonerExceptionUtils.replaceException;

public abstract class AbstractReflection implements Reflection {

    @Override
    public final Field[] getDeclaredFields(Class<?> type) {
        return replaceException(() -> getDeclaredFieldsImpl(type));
    }

    @Override
    public final Field getDeclaredField(Class<?> type, String name) {
        return replaceException(() -> getDeclaredFieldImpl(type, name));
    }

    @Override
    public final Method getDeclaredMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        return replaceException(() -> getDeclaredMethodImpl(type, name, parameterTypes));
    }

    @Override
    public final <T> Constructor<T> getDeclaredConstructor(Class<T> type, Class<?>... parameterTypes) {
        return replaceException(() -> getDeclaredConstructorImpl(type, parameterTypes));
    }

    @Override
    public final <T extends AccessibleObject> T makeAccessible(T member) {
        return replaceException(() -> makeAccessibleImpl(member));
    }

    protected Field getDeclaredFieldImpl(Class<?> type, String name) throws Exception {
        return Arrays.stream(getDeclaredFieldsImpl(type))
            .filter(field -> field.getName().equals(name))
            .findAny()
            .orElseThrow(() -> new NoSuchFieldException(String.format("No field '%s' in %s.", name, type)));
    }

    protected abstract Field[] getDeclaredFieldsImpl(Class<?> type) throws Exception;

    protected abstract Method getDeclaredMethodImpl(Class<?> type, String name, Class<?>... parameterTypes) throws Exception;

    protected abstract <T> Constructor<T> getDeclaredConstructorImpl(Class<T> type, Class<?>... parameterTypes) throws Exception;

    protected abstract <T extends AccessibleObject> T makeAccessibleImpl(T member);

}
