package org.sugarcubes.cloner;

/**
 * Two-phase copier.
 *
 * @author Maxim Butov
 */
public abstract class TwoPhaseObjectCopier<T> implements ObjectCopier<T> {

    @Override
    public final boolean isTrivial() {
        return false;
    }

    @Override
    public final T copy(T original, CopyContext context) {
        T clone = context.register(original, () -> allocate(original));
        context.fork(Executable.Void.of(() -> deepCopy(original, clone, context)));
        return clone;
    }

    /**
     * The first phase of copying. Creates an "empty" copy of the original object.
     *
     * @param original original object
     * @return clone
     */
    public abstract T allocate(T original);

    /**
     * The second phase of copying, copies the inner state from the original object into the clone.
     *
     * @param original original object
     * @param clone clone
     * @param context copying context
     */
    public abstract void deepCopy(T original, T clone, CopyContext context);

}
