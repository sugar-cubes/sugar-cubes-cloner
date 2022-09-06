/*
 * Copyright 2017-2022 the original author or authors.
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

import java.util.Queue;
import java.util.concurrent.Callable;
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
     * @param executor executor service
     */
    public ParallelCopyContext(CopierProvider copierProvider, ExecutorService executor) {
        super(copierProvider);
        this.executor = executor;
    }

    @Override
    public synchronized <T> void register(T original, T clone) {
        super.register(original, clone);
    }

    @Override
    public void thenInvoke(Callable<?> task) {
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
    public void complete() throws Exception {
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
                rethrow(e.getCause());
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

    private void rethrow(Throwable e) throws Exception {
        try {
            throw e;
        }
        catch (Error | Exception ex) {
            throw ex;
        }
        catch (Throwable ex) {
            throw new ClonerException(ex);
        }
    }

}
