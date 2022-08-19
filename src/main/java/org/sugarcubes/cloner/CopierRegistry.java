package org.sugarcubes.cloner;

/**
 * Copier registry interface.
 *
 * @author Maxim Butov
 */
public interface CopierRegistry {

    /**
     * Returns copier by type.
     *
     * @param type type
     * @return copier
     */
    ObjectCopier<?> getCopier(Class<?> type);

}
