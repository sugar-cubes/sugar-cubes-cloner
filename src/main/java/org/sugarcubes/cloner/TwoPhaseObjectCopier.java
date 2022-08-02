package org.sugarcubes.cloner;

/**
 * Two-phase cloner.
 *
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

    /**
     * The first phase of cloning. Creates an "empty" copy of the original object.
     *
     * @param original original object
     * @return clone
     * @throws Throwable if something went wrong
     */
    public abstract T allocate(T original) throws Throwable;

    /**
     * The second phase of cloning. Copying of the inner state from the original object into the clone.
     *
     * @param original original object
     * @param clone clone
     * @param context context
     * @throws Throwable if something went wrong
     */
    public abstract void deepCopy(T original, T clone, CopyContext context) throws Throwable;

}
