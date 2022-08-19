package org.sugarcubes.cloner;

import java.util.Queue;
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
     * @param registry copier registry
     * @param executor executor service
     */
    public ParallelCopyContext(CopierRegistry registry, ExecutorService executor) {
        super(registry);
        this.executor = executor;
    }

    @Override
    protected <T> T nonTrivialCopy(T original, ObjectCopier<T> copier) throws Exception {
        CopyResult<T> result;
        synchronized (original) {
            Object clone = clones.get(original);
            if (clone != null) {
                return (T) clone;
            }
            result = copier.copy(original, this);
            synchronized (clones) {
                clones.put(original, result.getObject());
            }
        }
        if (running) {
            result.ifHasNext(next -> futures.offer(executor.submit(next)));
        }
        return result.getObject();
    }

    @Override
    public void complete() throws Exception {
        for (Future<?> future; (future = futures.poll()) != null; ) {
            try {
                future.get();
            }
            catch (ExecutionException e) {
                running = false;
                futures.forEach(f -> f.cancel(false));
                try {
                    throw e.getCause();
                }
                catch (Error | Exception ex) {
                    throw ex;
                }
                catch (Throwable ex) {
                    throw new ClonerException(ex);
                }
            }
        }
    }

}
