package org.sugarcubes.cloner;

/**
 * Copier which always returns {@code null}.
 *
 * @author Maxim Butov
 */
public final class NullCopier<T> implements TrivialCopier<T> {

    @Override
    public T trivialCopy(T original) {
        return null;
    }

}
