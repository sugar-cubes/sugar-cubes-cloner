package org.sugarcubes.cloner;

import java.util.Map;

/**
 * Abstract copy context. Contains common code for the context implementations.
 *
 * @author Maxim Butov
 */
public abstract class AbstractCopyContext implements CompletableCopyContext {

    /**
     * Copiers registry.
     */
    protected final CopierRegistry registry;

    /**
     * Cache of previously copied objects.
     */
    protected final Map<Object, Object> clones = new FasterIdentityHashMap<>();

    /**
     * Creates context with specified copier registry.
     *
     * @param registry copier registry
     */
    protected AbstractCopyContext(CopierRegistry registry) {
        this.registry = registry;
    }

    @Override
    public <T> T copy(T original) throws Exception {
        if (original == null) {
            return null;
        }
        ObjectCopier<T> copier = registry.getCopier(original);
        if (copier instanceof TrivialCopier) {
            return ((TrivialCopier<T>) copier).trivialCopy(original);
        }
        return nonTrivialCopy(original, copier);
    }

    /**
     * Complex copying which must return non-null and non-original object.
     *
     * @param <T> object type
     * @param original original object
     * @param copier object copier
     * @return copy of the original object
     * @throws Exception if something went wrong
     */
    protected abstract <T> T nonTrivialCopy(T original, ObjectCopier<T> copier) throws Exception;

}
