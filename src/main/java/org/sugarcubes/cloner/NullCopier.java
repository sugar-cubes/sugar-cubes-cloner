package org.sugarcubes.cloner;

/**
 * Copier which always returns {@code null}.
 *
 * @author Maxim Butov
 */
public final class NullCopier<T> implements ObjectCopier<T> {

    @Override
    public boolean isTrivial() {
        return true;
    }

    @Override
    public T copy(T original, CopyContext context) throws Throwable {
        return null;
    }

}
