package org.sugarcubes.cloner;

/**
 * Object factory interface.
 *
 * @author Maxim Butov
 */
public interface ClonerObjectFactory {

    /**
     * Creates an instance of the specified class. Instance may be not initialized.
     *
     * @param clazz class to instantiate
     * @return new instance of the class
     * @throws ClonerException if something went wrong
     */
    default <T> T newInstance(Class<T> clazz) throws ClonerException {
        try {
            return newInstanceUnsafe(clazz);
        }
        catch (Error | ClonerException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new ClonerException("Instantiation error.", e);
        }
    }

    /**
     * Creates an instance of the specified class. Instance may be not initialized.
     * Same as {@link #newInstance(Class)} but can throw any exception.
     *
     * @param clazz class to instantiate
     * @return new instance of the class
     *
     * @throws Throwable if something went wrong
     */
    default <T> T newInstanceUnsafe(Class<T> clazz) throws Throwable {
        throw new AssertionError("Not implemented");
    }

}
