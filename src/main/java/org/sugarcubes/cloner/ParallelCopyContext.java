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
     * Running flag.
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
