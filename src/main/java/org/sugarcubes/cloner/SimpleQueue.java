package org.sugarcubes.cloner;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Simplified {@link java.util.Queue} implementation based on {@link ArrayDeque}. May be FIFO or LIFO queue.
 *
 * @param <T> item type
 *
 * @author Maxim Butov
 */
public abstract class SimpleQueue<T> {

    /**
     * Creates FIFO queue.
     *
     * @param <T> item type
     * @return FIFO queue
     */
    public static <T> SimpleQueue<T> fifo() {
        return new FifoQueue<>();
    }

    /**
     * Creates LIFO queue.
     *
     * @param <T> item type
     * @return LIFO queue
     */
    public static <T> SimpleQueue<T> lifo() {
        return new LifoQueue<>();
    }

    /**
     * Offers item into queue. Synonym of {@link java.util.Queue#offer(Object)}.
     *
     * @param item item
     */
    public abstract void offer(T item);

    /**
     * Offers all items into queue.
     *
     * @param items items
     */
    public void offer(Iterable<T> items) {
        items.forEach(this::offer);
    }

    /**
     * Polls item from queue. Synonym of {@link java.util.Queue#poll()}.
     *
     * @return item or {@code null} if queue is empty
     */
    public abstract T poll();

    public SimpleQueue<T> sync() {
        return new SyncQueue<>(this);
    }

    private static abstract class AbstractSimpleQueue<T> extends SimpleQueue<T> {

        /**
         * Deque implementation.
         */
        protected final Deque<T> queue = new ArrayDeque<>();

        public void offer(T item) {
            queue.offerLast(item);
        }

    }

    /**
     * FIFO queue implementation.
     *
     * @param <T> item type
     */
    private static final class FifoQueue<T> extends AbstractSimpleQueue<T> {

        @Override
        public T poll() {
            return queue.pollFirst();
        }

    }

    /**
     * LIFO queue implementation.
     *
     * @param <T> item type
     */
    private static final class LifoQueue<T> extends AbstractSimpleQueue<T> {

        @Override
        public T poll() {
            return queue.pollLast();
        }

    }

    private static final class SyncQueue<T> extends SimpleQueue<T> {

        private final SimpleQueue<T> target;

        public SyncQueue(SimpleQueue<T> target) {
            this.target = target;
        }

        @Override
        public synchronized void offer(T item) {
            target.offer(item);
        }

        @Override
        public synchronized void offer(Iterable<T> items) {
            target.offer(items);
        }

        @Override
        public synchronized T poll() {
            return target.poll();
        }

    }

}
