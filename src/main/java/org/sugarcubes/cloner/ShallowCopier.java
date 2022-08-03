package org.sugarcubes.cloner;

import java.lang.reflect.Method;

/**
 * One-phase copier which creates a shallow clone of the original object with {@link Object#clone()} method.
 *
 * @author Maxim Butov
 */
public final class ShallowCopier<T extends Cloneable> implements ObjectCopier<T> {

    /**
     * {@link Object#clone()} method.
     */
    private static final Method CLONE_METHOD = ReflectionUtils.getMethod(Object.class, "clone");

    @Override
    public boolean isTrivial() {
        return false;
    }

    @Override
    public T copy(T original, CopyContext context) throws Throwable {
        return context.register(original, (T) CLONE_METHOD.invoke(original));
    }

}
