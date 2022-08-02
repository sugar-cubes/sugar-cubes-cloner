package org.sugarcubes.cloner;

/**
 * Copier for the specific object type.
 *
 * @author Maxim Butov
 */
public interface ObjectCopier<T> {

    /**
     * Singleton instance of {@link NullCopier}.
     */
    ObjectCopier<?> NULL = new NullCopier<>();

    /**
     * Singleton instance of {@link NoopCopier}.
     */
    ObjectCopier<?> NOOP = new NoopCopier<>();

    /**
     * Singleton instance of {@link ShallowCopier}.
     */
    ObjectCopier<?> SHALLOW = new ShallowCopier<>();

    /**
     * Singleton instance of {@link ObjectArrayCopier}.
     */
    ObjectCopier<?> OBJECT_ARRAY = new ObjectArrayCopier();

    /**
     * The "trivial" means that repeated calling {@link #copy(Object, CopyContext)} is cheaper than saving
     * and requesting cloning result from {@link java.util.IdentityHashMap}.
     *
     * @return {@code true} if the copier is trivial
     */
    boolean isTrivial();

    /**
     * Creates a copy of the original object.
     *
     * @param original original object
     * @param context copying context or {@code null} if the copier is trivial
     * @return object copy
     * @throws Throwable if something went wrong
     */
    T copy(T original, CopyContext context) throws Throwable;

}
