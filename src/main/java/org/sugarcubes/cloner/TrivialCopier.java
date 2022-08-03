package org.sugarcubes.cloner;

/**
 * Copier which always returns original object.
 *
 * @author Maxim Butov
 */
public abstract class TrivialCopier<T> implements ObjectCopier<T> {

    @Override
    public final boolean isTrivial() {
        return true;
    }

    @Override
    public final T copy(T original, CopyContext context) throws Throwable {
        return trivialCopy(original);
    }

    /**
     * Returns trivial copy of the object (actually {@code null} or original).
     *
     * @param original original object
     * @return copy
     */
    public abstract T trivialCopy(T original);

}
