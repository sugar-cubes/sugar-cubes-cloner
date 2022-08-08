package org.sugarcubes.cloner;

/**
 * Base class for trivial copiers. "Trivial" means that repeated calling of the copy method is cheaper than saving
 * and requesting previous result from cache.
 *
 * @author Maxim Butov
 */
public abstract class TrivialCopier<T> implements ObjectCopier<T> {

    @Override
    public CopyResult<T> copy(T original, CopyContext context) {
        return new CopyResult<>(trivialCopy(original));
    }

    /**
     * Returns trivial copy of the object (actually {@code null} or original).
     *
     * @param original original object
     * @return copy
     */
    public abstract T trivialCopy(T original);

}
