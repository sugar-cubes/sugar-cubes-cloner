package org.sugarcubes.cloner;

/**
 * Base class for trivial copiers. "Trivial" means that repeated calling of the copy method is cheaper than saving
 * and requesting previous result from cache.
 *
 * @author Maxim Butov
 */
public interface TrivialCopier<T> extends ObjectCopier<T> {

    @Override
    default CopyResult<T> copy(T original, CopyContext context) {
        return new CopyResult<>(trivialCopy(original));
    }

    /**
     * Returns trivial copy of the object (actually {@code null} or original).
     *
     * @param original original object
     * @return copy
     */
    T trivialCopy(T original);

}
