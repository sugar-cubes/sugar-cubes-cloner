package org.sugarcubes.cloner;

import java.util.function.Supplier;

/**
 * Implementation of {@link Cloner}.
 *
 * @author Maxim Butov
 */
public class ClonerImpl implements Cloner {

    /**
     * Creates context for the single copy process.
     */
    private final Supplier<? extends AbstractCopyContext> contextSupplier;

    /**
     * Creates cloner with custom context supplier.
     *
     * @param contextSupplier context supplier
     */
    public ClonerImpl(Supplier<? extends AbstractCopyContext> contextSupplier) {
        this.contextSupplier = contextSupplier;
    }

    @Override
    public <T> T clone(T object) {
        try {
            AbstractCopyContext context = contextSupplier.get();
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
