package io.github.sugarcubes.cloner;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Jdk8ReflectionAdapterImpl implements ReflectionAdapter {

    @Override
    public <T> T instantiateClass(Class<T> type) throws Exception {
        return type.newInstance();
    }

    @Override
    public Field[] getFields(Class<?> type) throws Exception {
        return type.getDeclaredFields();
    }

    @Override
    public Field getField(Class<?> type, String fieldName) throws Exception {
        return type.getField(fieldName);
    }

    @Override
    public <T> Constructor<T> getConstructor(Class<T> type, Class<?>... parameterTypes) throws Exception {
        return type.getDeclaredConstructor(parameterTypes);
    }

    @Override
    public Method getMethod(Class<?> type, String methodName, Class<?>... parameterTypes) throws Exception {
        return type.getDeclaredMethod(methodName, parameterTypes);
    }

    @Override
    public void makeAccessible(AccessibleObject element) throws Exception {
        element.setAccessible(true);
    }

}
