package org.sugarcubes.cloner;

/**
 * Two-phase copier.
 *
 * @author Maxim Butov
 */
public interface TwoPhaseObjectCopier<T> extends ObjectCopier<T> {

    @Override
    default CopyResult<T> copy(T original, CopyContext context) throws Exception {
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
    T allocate(T original) throws Exception;

    /**
     * The second phase of copying, copies the inner state from the original object into the clone.
     *
     * @param original original object
     * @param clone clone
     * @param context copying context
     * @throws Exception if something went wrong
     */
    void deepCopy(T original, T clone, CopyContext context) throws Exception;

}
