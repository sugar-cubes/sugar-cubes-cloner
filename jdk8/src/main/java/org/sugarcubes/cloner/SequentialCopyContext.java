/*
 * Copyright 2017-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sugarcubes.cloner;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Copy context for sequential copying.
 *
 * @author Maxim Butov
 */
public class SequentialCopyContext extends AbstractSingleThreadCopyContext {

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
     * @param copierProvider copier provider
     * @param clones predefined cloned objects
     * @param traversalAlgorithm traversal algorithm
     */
    public SequentialCopyContext(CopierProvider copierProvider, Map<Object, Object> clones, TraversalAlgorithm traversalAlgorithm) {
        super(copierProvider, clones);
        this.poll = getPollMethod(traversalAlgorithm);
    }

    /**
     * Returns queue poll method for traversal algorithm.
     *
     * @param <T> queue item type
     * @param traversalAlgorithm traversal algorithm
     * @return queue poll method
     */
    private <T> Function<Deque<T>, T> getPollMethod(TraversalAlgorithm traversalAlgorithm) {
        switch (traversalAlgorithm) {
            case DEPTH_FIRST:
                return Deque::pollLast;
            case BREADTH_FIRST:
                return Deque::pollFirst;
            default:
                throw Checks.neverHappens();
        }
    }

    @Override
    public void thenInvoke(Callable<?> task) throws Exception {
        queue.offer(task);
    }

    @Override
    public void complete() throws Throwable {
        Deque<Callable<?>> queue = this.queue;
        Function<Deque<Callable<?>>, Callable<?>> poll = this.poll;
        for (Callable<?> next; (next = poll.apply(queue)) != null; ) {
            next.call();
        }
    }

}
