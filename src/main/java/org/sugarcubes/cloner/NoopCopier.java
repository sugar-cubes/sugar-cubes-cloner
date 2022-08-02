package org.sugarcubes.cloner;

/**
 * Copier which always returns original object.
 *
 * @author Maxim Butov
 */
public final class NoopCopier<T> implements ObjectCopier<T> {

    @Override
    public boolean isTrivial() {
        return true;
    }

    @Override
    public T copy(T original, CopyContext context) throws Throwable {
        return original;
    }

}
