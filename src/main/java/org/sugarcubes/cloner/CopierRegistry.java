package org.sugarcubes.cloner;

/**
 * Copier registry interface.
 *
 * @author Maxim Butov
 */
@FunctionalInterface
public interface CopierRegistry {

    /**
     * Returns copier by type.
     *
     * @param type type
     * @return copier
     * @param <T> object type
     */
    <T> ObjectCopier<T> getCopier(Class<T> type);

    /**
     * Returns copier by original object.
     *
     * @param original original object
     * @return copier
     * @param <T> object type
     */
    default <T> ObjectCopier<T> getCopier(T original) {
        return getCopier((Class<T>) original.getClass());
    }

}
