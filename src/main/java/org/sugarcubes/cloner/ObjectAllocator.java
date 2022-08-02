package org.sugarcubes.cloner;

/**
 * Object allocator interface.
 *
 * @author Maxim Butov
 */
public interface ObjectAllocator {

    /**
     * Creates an instance of the specified class. Instance may be not initialized.
     *
     * @param clazz class to instantiate
     * @return new instance of the class
     * @throws Throwable if something went wrong
     */
    <T> T newInstance(Class<T> clazz) throws Throwable;

}
