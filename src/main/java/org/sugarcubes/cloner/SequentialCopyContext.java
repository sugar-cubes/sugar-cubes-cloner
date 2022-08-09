package org.sugarcubes.cloner;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Maxim Butov
 */
public final class SequentialCopyContext implements CopyContext {

    private final TraversalAlgorithm traversalAlgorithm;
    private final CopierRegistry registry;

    private final Deque<Callable<?>> queue = new ArrayDeque<>();
    private final Map<Object, Object> clones = new FasterIdentityHashMap<>();

    public SequentialCopyContext(TraversalAlgorithm traversalAlgorithm, CopierRegistry registry) {
        this.traversalAlgorithm = traversalAlgorithm;
        this.registry = registry;
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
        result.getNext().forEach(queue::offer);
        return clone;
    }

    @Override
    public void complete() throws Exception {
        for (Callable<?> next; (next = traversalAlgorithm.poll(queue)) != null; ) {
            next.call();
        }
    }

}
