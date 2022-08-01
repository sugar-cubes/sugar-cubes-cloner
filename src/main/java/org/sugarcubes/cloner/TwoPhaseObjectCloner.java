package org.sugarcubes.cloner;

/**
 * @author Maxim Butov
 */
public abstract class TwoPhaseObjectCloner<T> implements ObjectCloner<T> {

    @Override
    public final boolean isTrivial() {
        return false;
    }

    @Override
    public final T clone(T original, ClonerContext context) throws Throwable {
        T clone = allocate(original);
        context.invokeLater(() -> deepCopy(original, clone, context));
        return clone;
    }

    public abstract T allocate(T original) throws Throwable;

    public abstract void deepCopy(T original, T clone, ClonerContext context) throws Throwable;

}
