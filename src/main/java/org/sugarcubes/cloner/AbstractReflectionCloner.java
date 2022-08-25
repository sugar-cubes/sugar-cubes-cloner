package org.sugarcubes.cloner;

/**
 * Abstract base implementation of {@link Cloner} which uses Java reflection API for cloning.
 *
 * @author Maxim Butov
 */
public abstract class AbstractReflectionCloner implements Cloner {

    /**
     * Creates context for the single copy process.
     *
     * @return copy context
     */
    protected abstract CopyContext newCopyContext();

    @Override
    public <T> T clone(T object) {
        try {
            CopyContext context = newCopyContext();
            T clone = context.copy(object);
            context.complete();
            return clone;
        }
        catch (ClonerException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ClonerException(e);
        }
    }

}
