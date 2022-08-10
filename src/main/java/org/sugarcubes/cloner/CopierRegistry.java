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
    ObjectCopier<?> getUntypedCopier(Class<?> type);

    /**
     * Returns typed copier by type.
     *
     * @param type type
     * @param <T> object type
     * @return copier
     */
    default <T> ObjectCopier<T> getCopier(Class<T> type) {
        return (ObjectCopier<T>) getUntypedCopier(type);
    }

    /**
     * Returns typed copier by original object.
     *
     * @param original original object
     * @return copier
     * @param <T> object type
     */
    default <T> ObjectCopier<T> getCopier(T original) {
        return getCopier((Class<T>) original.getClass());
    }

}
