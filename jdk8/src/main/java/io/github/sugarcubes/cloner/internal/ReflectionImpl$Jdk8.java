package io.github.sugarcubes.cloner.internal;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionImpl$Jdk8 extends AbstractReflection {

    @Override
    protected Field getDeclaredFieldImpl(Class<?> type, String name) throws Exception {
        return type.getDeclaredField(name);
    }

    @Override
    protected Field[] getDeclaredFieldsImpl(Class<?> type) throws Exception {
        return type.getDeclaredFields();
    }

    @Override
    protected Method getDeclaredMethodImpl(Class<?> type, String name, Class<?>... parameterTypes) throws Exception {
        return type.getDeclaredMethod(name, parameterTypes);
    }

    @Override
    protected <T> Constructor<T> getDeclaredConstructorImpl(Class<T> type, Class<?>... parameterTypes) throws Exception {
        return type.getDeclaredConstructor(parameterTypes);
    }

    @Override
    protected <T extends AccessibleObject> T makeAccessibleImpl(T member) {
        member.setAccessible(true);
        return member;
    }

}
