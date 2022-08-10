package org.sugarcubes.cloner;

/**
 * Two-phase copier.
 *
 * @author Maxim Butov
 */
public abstract class TwoPhaseObjectCopier<T> implements ObjectCopier<T> {

    @Override
    public CopyResult<T> copy(T original, CopyContext context) throws Exception {
        T clone = allocate(original);
        return new CopyResult<>(
            clone,
            () -> {
                deepCopy(original, clone, context);
                return null;
            }
        );
    }

    /**
     * The first phase of copying. Creates an "empty" copy of the original object.
     *
     * @param original original object
     * @return clone
     * @throws Exception if something went wrong
     */
    public abstract T allocate(T original) throws Exception;

    /**
     * The second phase of copying, copies the inner state from the original object into the clone.
     *
     * @param original original object
     * @param clone clone
     * @param context copying context
     * @throws Exception if something went wrong
     */
    public abstract void deepCopy(T original, T clone, CopyContext context) throws Exception;

}
