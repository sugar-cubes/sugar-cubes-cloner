package org.sugarcubes.cloner;

/**
 * Base abstract class for cloners.
 *
 * @author Maxim Butov
 */
public abstract class AbstractCloner implements Cloner {

    @Override
    public <T> T clone(T object) {
        try {
            return (T) doClone(object);
        }
        catch (Error | ClonerException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new ClonerException("Cloning error.", e);
        }
    }

    /**
     * Performs cloning of the object.
     *
     * @param object object to clone, not null
     * @return clone of the object
     * @throws Throwable if something went wrong
     */
    protected abstract Object doClone(Object object) throws Throwable;

}
