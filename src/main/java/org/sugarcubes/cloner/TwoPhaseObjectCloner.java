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
    public final T clone(T original, LaterSupport laterSupport) throws Throwable {
        T clone = allocate(original);
        laterSupport.invokeLater(context -> deepCopy(original, clone, context));
        return clone;
    }

    public abstract T allocate(T original) throws Throwable;

    public abstract void deepCopy(T original, T clone, ClonerContext context) throws Throwable;

}
