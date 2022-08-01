package org.sugarcubes.cloner.impl;

/**
 * @author Maxim Butov
 */
public abstract class TwoPhaseObjectCopier<T> implements ObjectCopier<T> {

    @Override
    public final boolean isTrivial() {
        return false;
    }

    @Override
    public final T copy(T original, CopyContext context) throws Throwable {
        T clone = allocate(original);
        context.invokeLater(() -> deepCopy(original, clone, context));
        return clone;
    }

    public abstract T allocate(T original) throws Throwable;

    public abstract void deepCopy(T original, T clone, CopyContext context) throws Throwable;

}
