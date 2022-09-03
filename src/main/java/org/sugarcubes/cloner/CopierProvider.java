package org.sugarcubes.cloner;

/**
 * Copier provider interface.
 *
 * @author Maxim Butov
 */
public interface CopierProvider {

    /**
     * Returns copier for the object.
     *
     * @param original original object
     * @return copier
     */
    <T> ObjectCopier<T> getCopier(T original);

}
