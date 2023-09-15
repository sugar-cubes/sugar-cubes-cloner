package io.github.sugarcubes.cloner;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class ReflectionAdapterImpl$jdk9 extends ReflectionAdapterImpl {

    @Override
    public Field[] getFields(Class<?> type) throws Exception {
        makeAccessible(type);
        return super.getFields(type);
    }

    @Override
    public Field getField(Class<?> type, String fieldName) throws Exception {
        makeAccessible(type);
        return super.getField(type, fieldName);
    }

    @Override
    public <T> Constructor<T> getConstructor(Class<T> type, Class<?>... parameterTypes) throws Exception {
        makeAccessible(type);
        return super.getConstructor(type, parameterTypes);
    }

    @Override
    public Method getMethod(Class<?> type, String methodName, Class<?>... parameterTypes) throws Exception {
        makeAccessible(type);
        return super.getMethod(type, methodName, parameterTypes);
    }

    @Override
    public void makeAccessible(AccessibleObject element) throws Exception {
        makeAccessible(((Member) element).getDeclaringClass());
        super.makeAccessible(element);
    }

    private final Set<Class<?>> accessibleClassed = Collections.newSetFromMap(new WeakHashMap<>());

    protected void makeAccessible(Class<?> type) throws Exception {
        if (accessibleClassed.add(type)) {

        }
    }

}
