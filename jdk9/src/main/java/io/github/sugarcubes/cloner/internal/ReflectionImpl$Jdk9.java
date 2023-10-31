package io.github.sugarcubes.cloner.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionImpl$Jdk9 extends ReflectionImpl$Jdk8 {

    private final TypeOpener opener;

    public ReflectionImpl$Jdk9(TypeOpener opener) {
        this.opener = opener;
    }

    @Override
    protected Field getDeclaredFieldImpl(Class<?> type, String name) throws Exception {
        opener.open(type);
        return super.getDeclaredFieldImpl(type, name);
    }

    @Override
    protected Field[] getDeclaredFieldsImpl(Class<?> type) throws Exception {
        opener.open(type);
        return super.getDeclaredFieldsImpl(type);
    }

    @Override
    protected Method getDeclaredMethodImpl(Class<?> type, String name, Class<?>... parameterTypes) throws Exception {
        opener.open(type);
        return super.getDeclaredMethodImpl(type, name, parameterTypes);
    }

    @Override
    protected <T> Constructor<T> getDeclaredConstructorImpl(Class<T> type, Class<?>... parameterTypes) throws Exception {
        opener.open(type);
        return super.getDeclaredConstructorImpl(type, parameterTypes);
    }

}
