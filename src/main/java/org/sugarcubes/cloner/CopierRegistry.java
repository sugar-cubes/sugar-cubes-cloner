package org.sugarcubes.cloner;

/**
 * Copier registry interface.
 *
 * @author Maxim Butov
 */
@FunctionalInterface
public interface CopierRegistry {

    /**
     * Returns untyped copier by type.
     *
     * @param type type
     * @return copier
     */
    ObjectCopier<?> getCopier(Class<?> type);

    /**
     * Returns typed copier by type.
     *
     * @param type type
     * @param <T> object type
     * @return copier
     */
    default <T> ObjectCopier<T> getTypedCopier(Class<T> type) {
        return (ObjectCopier<T>) getCopier(type);
    }

    /**
     * Returns copier by original object.
     *
     * @param original original object
     * @return copier
     * @param <T> object type
     */
    default <T> ObjectCopier<T> getCopier(T original) {
        return getTypedCopier((Class<T>) original.getClass());
    }

}
