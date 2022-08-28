package org.sugarcubes.cloner;

/**
 * Copier provider interface.
 *
 * @author Maxim Butov
 */
public interface CopierProvider {

    /**
     * Returns copier by type.
     *
     * @param type type
     * @return copier
     */
    ObjectCopier<?> getCopier(Class<?> type);

}
