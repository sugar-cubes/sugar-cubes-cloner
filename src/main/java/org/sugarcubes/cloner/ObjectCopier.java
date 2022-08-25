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
     * Creates a copy of the original object.
     *
     * @param original original object
     * @param context copying context or {@code null} if the copier is trivial
     * @return copy result
     * @throws Exception if something went wrong
     */
    T copy(T original, CopyContext context) throws Exception;

}
