package org.sugarcubes.cloner.impl;

import java.lang.reflect.Method;

/**
 * @author Maxim Butov
 */
public final class ShallowCopier<T extends Cloneable> implements ObjectCopier<T> {

    private static final Method CLONE_METHOD = ReflectionUtils.getMethod(Object.class, "clone");

    @Override
    public boolean isTrivial() {
        return false;
    }

    @Override
    public T copy(T original, CopyContext context) throws Throwable {
        return (T) CLONE_METHOD.invoke(original);
    }

}
