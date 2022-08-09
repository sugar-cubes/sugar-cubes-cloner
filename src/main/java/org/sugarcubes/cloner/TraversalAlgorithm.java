package org.sugarcubes.cloner;

import java.util.Deque;
import java.util.concurrent.BlockingDeque;

/**
 * Object graph traversal algorithm.
 *
 * @author Maxim Butov
 */
public enum TraversalAlgorithm {

    /**
     * Depth first.
     */
    DEPTH_FIRST {
        @Override
        public <T> T poll(Deque<T> queue) {
            return queue.pollLast();
        }

        @Override
        public <T> T take(BlockingDeque<T> queue) throws InterruptedException {
            return queue.takeLast();
        }
    },

    /**
     * Breadth first.
     */
    BREADTH_FIRST {
        @Override
        public <T> T poll(Deque<T> queue) {
            return queue.pollFirst();
        }

        @Override
        public <T> T take(BlockingDeque<T> queue) throws InterruptedException {
            return queue.takeFirst();
        }
    };

    /**
     * Polls item from deque depending on algorithm.
     *
     * @param queue double-ended queue
     * @param <T> item type
     * @return item or {@code null}
     */
    public abstract <T> T poll(Deque<T> queue);

    /**
     * Takes item from blocking deque depending on algorithm.
     *
     * @param queue double-ended blocking queue
     * @param <T> item type
     * @return item or {@code null}
     */
    public abstract <T> T take(BlockingDeque<T> queue) throws InterruptedException;

}
