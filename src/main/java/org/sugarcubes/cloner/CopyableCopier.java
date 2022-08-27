package org.sugarcubes.cloner;

/**
 * Copier for {@link Copyable} objects.
 *
 * @author Maxim Butov
 */
public final class CopyableCopier<T extends Copyable<T>> implements ObjectCopier<T> {

    @Override
    public T copy(T original, CopyContext context) throws Exception {
        return original.copy(context);
    }

}
