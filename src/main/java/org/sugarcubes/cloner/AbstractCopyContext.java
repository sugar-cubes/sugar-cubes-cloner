package org.sugarcubes.cloner;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Abstract copy context. Contains common code for the context implementations.
 *
 * @author Maxim Butov
 */
public abstract class AbstractCopyContext implements CopyContext {

    /**
     * Copiers registry.
     */
    private final CopierRegistry registry;

    /**
     * Cache of previously copied objects.
     */
    private final Map<Object, Object> clones = new IdentityHashMap<>();

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
        ObjectCopier<T> copier = (ObjectCopier<T>) registry.getCopier(original.getClass());
        // trivial case
        if (copier == ObjectCopier.NULL || copier == ObjectCopier.NOOP) {
            return copier.copy(original, this);
        }
        return doClone(original, copier);
    }

    @Override
    public <T> void register(T original, T clone) {
        clones.put(original, clone);
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
    protected <T> T doClone(T original, ObjectCopier<T> copier) throws Exception {
        T clone = (T) clones.get(original);
        return clone != null ? clone : copier.copy(original, this);
    }

    /**
     * Completes all the delayed tasks.
     *
     * @throws Exception if something went wrong
     */
    public abstract void complete() throws Exception;

}
