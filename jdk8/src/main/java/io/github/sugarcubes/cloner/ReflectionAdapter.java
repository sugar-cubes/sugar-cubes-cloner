package io.github.sugarcubes.cloner;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface ReflectionAdapter {

    <T> T instantiateClass(Class<T> type) throws Exception;

    Field[] getFields(Class<?> type) throws Exception;

    Field getField(Class<?> type, String fieldName) throws Exception;

    <T> Constructor<T> getConstructor(Class<T> type, Class<?>... parameterTypes) throws Exception;

    Method getMethod(Class<?> type, String methodName, Class<?>... parameterTypes) throws Exception;

    void makeAccessible(AccessibleObject element) throws Exception;

}
