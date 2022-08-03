package org.sugarcubes.cloner;

/**
 * Copier which always returns original object.
 *
 * @author Maxim Butov
 */
public final class NoopCopier<T> extends TrivialCopier<T> {

    @Override
    public T trivialCopy(T original) {
        return original;
    }

}
