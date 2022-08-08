package org.sugarcubes.cloner;

import java.util.function.Supplier;

/**
 * Object graph traversal algorithm.
 *
 * @author Maxim Butov
 */
public enum TraversalAlgorithm {

    /**
     * Depth first.
     */
    DEPTH_FIRST(SimpleQueue::lifo),

    /**
     * Breadth first.
     */
    BREADTH_FIRST(SimpleQueue::fifo);

    private final Supplier<SimpleQueue<?>> queueSupplier;

    TraversalAlgorithm(Supplier<SimpleQueue<?>> queueSupplier) {
        this.queueSupplier = queueSupplier;
    }

    public <T> SimpleQueue<T> createQueue() {
        return (SimpleQueue) queueSupplier.get();
    }

}
