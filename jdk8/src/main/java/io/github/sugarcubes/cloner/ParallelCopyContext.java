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
package io.github.sugarcubes.cloner;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

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
     * Creates an instance.
     *
     * @param copierProvider copier provider
     * @param clones predefined cloned objects
     * @param executor executor service
     */
    public ParallelCopyContext(CopierProvider copierProvider, Map<Object, Object> clones, ExecutorService executor) {
        super(copierProvider, ConcurrentHashMap::new, clones);
        this.executor = executor;
    }

    @Override
    public void thenInvoke(Callable<?> task) throws Exception {
        if (running) {
            futures.offer(executor.submit(task));
        }
    }

    @Override
    protected <T> T doClone(T original, ObjectCopier<T> copier) throws Exception {
        synchronized (original) {
            return super.doClone(original, copier);
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

}
