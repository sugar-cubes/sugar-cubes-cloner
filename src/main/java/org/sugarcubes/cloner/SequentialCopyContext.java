package org.sugarcubes.cloner;

import java.util.ArrayDeque;
import java.util.Deque;
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
    private final Deque<ContextAction> queue = new ArrayDeque<>();

    /**
     * Poll method for {@link #queue}.
     * Depending on traversal algorithm can be {@link Deque#pollLast()} or {@link Deque#pollFirst()}.
     */
    private final Function<Deque<ContextAction>, ContextAction> poll;

    /**
     * Creates an object instance.
     *
     * @param copierProvider copier provider
     * @param traversalAlgorithm traversal algorithm
     */
    public SequentialCopyContext(CopierProvider copierProvider, TraversalAlgorithm traversalAlgorithm) {
        super(copierProvider);
        this.poll = getPollMethod(traversalAlgorithm);
    }

    private <T> Function<Deque<T>, T> getPollMethod(TraversalAlgorithm traversalAlgorithm) {
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
    public void thenInvoke(ContextAction task) {
        queue.offer(task);
    }

    @Override
    public void complete() throws Exception {
        for (ContextAction next; (next = poll.apply(queue)) != null; ) {
            next.perform();
        }
    }

}
