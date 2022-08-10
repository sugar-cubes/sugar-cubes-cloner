package org.sugarcubes.cloner;

/**
 * Factory for objects of the specific type.
 *
 * @author Maxim Butov
 */
public interface ObjectFactory<T> {

    /**
     * Creates an object instance. Instance may be not initialized.
     *
     * @return new object instance
     * @throws Exception if something went wrong
     */
    T newInstance() throws Exception;

}
