package org.sugarcubes.cloner;

import java.lang.reflect.Method;

/**
 * @author Maxim Butov
 */
public final class ShallowCloner<T extends Cloneable> implements ObjectCloner<T> {

    private static final Method CLONE_METHOD = ReflectionUtils.getMethod(Object.class, "clone");

    @Override
    public boolean isTrivial() {
        return false;
    }

    @Override
    public T clone(T original, ClonerContext context) throws Throwable {
        return (T) CLONE_METHOD.invoke(original);
    }

}
