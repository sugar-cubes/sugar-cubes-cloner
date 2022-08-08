package org.sugarcubes.cloner;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Maxim Butov
 */
public final class SequentialCopyContext implements CopyContext {

    private final CopierRegistry registry;
    private final SimpleQueue<Callable<?>> queue;
    private final Map<Object, Object> clones = new FasterIdentityHashMap<>();

    public SequentialCopyContext(CopierRegistry registry, TraversalAlgorithm traversalAlgorithm) {
        this.registry = registry;
        this.queue = traversalAlgorithm.createQueue();
    }

    @Override
    public <U> U copy(U original) throws Exception {
        if (original == null) {
            return null;
        }
        ObjectCopier<U> typeCloner = registry.getCopier(original);
        if (typeCloner instanceof TrivialCopier) {
            return ((TrivialCopier<U>) typeCloner).trivialCopy(original);
        }
        U clone = (U) clones.get(original);
        if (clone != null) {
            return clone;
        }
        CopyResult<U> result = typeCloner.copy(original, this);
        clone = result.getObject();
        clones.put(original, clone);
        queue.offer(result.getNext());
        return clone;
    }

    @Override
    public void complete() throws Exception {
        for (Callable<?> next; (next = queue.poll()) != null; ) {
            next.call();
        }
    }

}
