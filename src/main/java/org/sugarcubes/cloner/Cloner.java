package org.sugarcubes.cloner;

/**
 * Cloner interface.
 *
 * @author Maxim Butov
 */
public interface Cloner {

    /**
     * Creates a deep clone of the object.
     *
     * @param <T> object type
     * @param object object to clone
     * @return a clone
     * @throws ClonerException if something went wrong
     */
    <T> T clone(T object) throws ClonerException;

}
