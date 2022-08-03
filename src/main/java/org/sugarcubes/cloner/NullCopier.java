package org.sugarcubes.cloner;

/**
 * Copier which always returns {@code null}.
 *
 * @author Maxim Butov
 */
public final class NullCopier<T> extends TrivialCopier<T> {

    @Override
    public T trivialCopy(T original) {
        return null;
    }

}
