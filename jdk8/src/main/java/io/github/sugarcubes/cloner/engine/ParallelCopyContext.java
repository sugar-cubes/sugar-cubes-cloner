/*
 * Copyright 2017-2024 the original author or authors.
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
package io.github.sugarcubes.cloner.engine;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Predicate;

/**
 * Copy context for parallel copying.
 *
 * @author Maxim Butov
 */
public class ParallelCopyContext extends AbstractCopyContext {

    /**
     * Executor service.
     */
    private final ExecutorService executor;

    /**
     * Queue of the futures.
     */
    private final Queue<Future<?>> futures = new ConcurrentLinkedDeque<>();

    /**
     * Running flag. When set to {@code false} further tasks will not be executed.
     */
    private volatile boolean running = true;

    /**
     * Slot whose second phase the thread is running.
     */
    private final ThreadLocal<CloneSlot> owner = new ThreadLocal<>();

    /**
     * Maximal time of a single wait, after which the waiting thread checks for a reference cycle again.
     */
    private static final long CYCLE_CHECK_INTERVAL_MILLIS = 100;

    /**
     * Slots which threads are waiting for, created on the first wait.
     */
    private Map<Thread, CloneSlot> waits;

    /**
     * Creates an instance.
     *
     * @param copierProvider copier provider
     * @param clones predefined cloned objects
     * @param executor executor service
     */
    public ParallelCopyContext(CopierProvider copierProvider, Map<Object, Object> clones, ExecutorService executor) {
        super(copierProvider, new ConcurrentSlotMap(), clones);
        this.executor = executor;
    }

    @Override
    CloneSlot newSlot(Object original) {
        return new ConcurrentCloneSlot(original);
    }

    /**
     * Depth of nested copy constructors run by the thread.
     */
    private final ThreadLocal<int[]> constructionDepth = ThreadLocal.withInitial(() -> new int[1]);

    @Override
    int[] constructionDepth() {
        return constructionDepth.get();
    }

    @Override
    CloneSlot getOwner() {
        return owner.get();
    }

    @Override
    void setOwner(CloneSlot owner) {
        if (owner != null) {
            this.owner.set(owner);
        }
        else {
            this.owner.remove();
        }
    }

    @Override
    public void thenInvoke(Callable<?> task) throws Exception {
        if (running) {
            futures.offer(executor.submit(task));
        }
    }

    @Override
    public void complete() throws Throwable {
        Queue<Future<?>> futures = this.futures;
        for (Future<?> future; (future = futures.poll()) != null; ) {
            try {
                future.get();
            }
            catch (InterruptedException e) {
                cancel();
                Thread.currentThread().interrupt();
                return;
            }
            catch (ExecutionException e) {
                cancel();
                throw e.getCause();
            }
        }
    }

    private void cancel() {
        running = false;
        Queue<Future<?>> futures = this.futures;
        for (Future<?> future; (future = futures.poll()) != null; ) {
            future.cancel(false);
        }
    }

    /**
     * Waits for other threads; a thread which has to wait for something which in turn waits for this thread (through
     * busy threads, holes and waiting threads) has found a reference cycle which cannot be copied.
     */
    @Override
    boolean await(CloneSlot slot, Object original, Predicate<CloneSlot> ready) throws InterruptedException {
        Thread current = Thread.currentThread();
        Map<Thread, CloneSlot> waits = waits();
        waits.put(current, slot);
        try {
            while (!ready.test(slot)) {
                if (slot.isFailed()) {
                    throw failed(original);
                }
                if (slot.isClaimable()) {
                    return true;
                }
                if (dependsOn(slot, current, waits)) {
                    return false;
                }
                slot.awaitChange(ready, CYCLE_CHECK_INTERVAL_MILLIS);
            }
            return true;
        }
        finally {
            waits.remove(current);
        }
    }

    private synchronized Map<Thread, CloneSlot> waits() {
        if (waits == null) {
            waits = new ConcurrentHashMap<>();
        }
        return waits;
    }

    /**
     * Tells whether the slot waits, directly or transitively, for the given thread: through the thread busy with a
     * slot, the holes of a slot and the slot a thread waits for.
     */
    private static boolean dependsOn(CloneSlot start, Thread thread, Map<Thread, CloneSlot> waits) {
        Set<CloneSlot> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<CloneSlot> pending = new ArrayDeque<>();
        pending.push(start);
        while (!pending.isEmpty()) {
            CloneSlot slot = pending.pop();
            if (!visited.add(slot)) {
                continue;
            }
            Thread busy = slot.getBusyThread();
            if (busy == thread) {
                return true;
            }
            if (busy != null) {
                CloneSlot awaited = waits.get(busy);
                if (awaited != null) {
                    pending.push(awaited);
                }
            }
            for (CloneSlot blocker : slot.getBlockers()) {
                pending.push(blocker);
            }
        }
        return false;
    }

}
