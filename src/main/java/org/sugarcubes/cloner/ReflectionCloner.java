package org.sugarcubes.cloner;

import java.util.function.Supplier;

/**
 * Abstract base implementation of {@link Cloner} which uses Java reflection API for cloning.
 *
 * @author Maxim Butov
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class ReflectionCloner implements Cloner {

    /**
     * Copy context factory.
     */
    private final Supplier<CompletableCopyContext> contextFactory;

    /**
     * Constructor.
     *
     * @param contextFactory context factory
     */
    public ReflectionCloner(Supplier<CompletableCopyContext> contextFactory) {
        this.contextFactory = contextFactory;
    }

    @Override
    public <T> T clone(T object) {
        try {
            CompletableCopyContext context = contextFactory.get();
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
