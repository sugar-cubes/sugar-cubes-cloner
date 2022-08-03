package org.sugarcubes.cloner;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Simplified {@link java.util.Queue} implementation. May be FIFO or LIFO queue.
 *
 * @author Maxim Butov
 */
public final class SimpleQueue<T> {

    /**
     * Creates FIFO queue based on {@link LinkedList}.
     *
     * @param <T> object type
     * @return FIFO queue
     */
    public static <T> SimpleQueue<T> fifo() {
        return new SimpleQueue<>(new LinkedList<>(), Deque::offerLast, Deque::pollFirst);
    }

    /**
     * Creates LIFO queue based on {@link ArrayDeque}.
     *
     * @param <T> object type
     * @return LIFO queue
     */
    public static <T> SimpleQueue<T> lifo() {
        return new SimpleQueue<>(new ArrayDeque<>(), Deque::offerLast, Deque::pollLast);
    }

    /**
     * Deque implementation.
     */
    private final Deque<T> queue;

    /**
     * Offer method reference.
     */
    private final BiConsumer<Deque<T>, T> offerMethod;

    /**
     * Poll method reference.
     */
    private final Function<Deque<T>, T> pollMethod;

    /**
     * Constructor.
     *
     * @param queue deque implementation
     * @param offerMethod offer method reference
     * @param pollMethod poll method reference
     */
    public SimpleQueue(Deque<T> queue, BiConsumer<Deque<T>, T> offerMethod, Function<Deque<T>, T> pollMethod) {
        this.queue = queue;
        this.offerMethod = offerMethod;
        this.pollMethod = pollMethod;
    }

    /**
     * Offers item into queue. Synonym of {@link java.util.Queue#offer(Object)}.
     *
     * @param item item
     */
    public void offer(T item) {
        offerMethod.accept(queue, item);
    }

    /**
     * Polls item from queue. Synonym of {@link java.util.Queue#poll()}.
     *
     * @return item or {@code null} if queue is empty
     */
    public T poll() {
        return pollMethod.apply(queue);
    }

}
