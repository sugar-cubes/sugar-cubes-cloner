package org.sugarcubes.cloner;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Copy context for sequential copying.
 *
 * @author Maxim Butov
 */
public class SequentialCopyContext extends AbstractCopyContext {

    /**
     * Queue of actions to complete copying.
     */
    private final Deque<Callable<?>> queue = new ArrayDeque<>();

    /**
     * Poll method for {@link #queue}.
     * Depending on traversal algorithm can be {@link Deque#pollLast()} or {@link Deque#pollFirst()}.
     */
    private final Function<Deque<Callable<?>>, Callable<?>> poll;

    /**
     * Creates an object instance.
     *
     * @param registry copier registry
     * @param traversalAlgorithm traversal algorithm
     */
    public SequentialCopyContext(CopierRegistry registry, TraversalAlgorithm traversalAlgorithm) {
        super(registry);
        this.poll = getPollMethod(traversalAlgorithm);
    }

    private Function<Deque<Callable<?>>, Callable<?>> getPollMethod(TraversalAlgorithm traversalAlgorithm) {
        switch (traversalAlgorithm) {
            case DEPTH_FIRST:
                return Deque::pollLast;
            case BREADTH_FIRST:
                return Deque::pollFirst;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    protected <T> T nonTrivialCopy(T original, ObjectCopier<T> copier) throws Exception {
        T clone = (T) clones.get(original);
        if (clone != null) {
            return clone;
        }
        CopyResult<T> result = copier.copy(original, this);
        clone = result.getObject();
        clones.put(original, clone);
        result.ifHasNext(queue::offer);
        return clone;
    }

    @Override
    public void complete() throws Exception {
        for (Callable<?> next; (next = poll.apply(queue)) != null; ) {
            next.call();
        }
    }

}
