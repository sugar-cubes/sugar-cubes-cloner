package org.sugarcubes.cloner;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Maxim Butov
 */
public class ParallelCopyContext implements CopyContext {

    private static class BreakException extends Exception {

    }

    private static final Callable<?> BREAK = () -> {
        throw new BreakException();
    };

    private final TraversalAlgorithm traversalAlgorithm;
    private final CopierRegistry registry;

    private final BlockingDeque<Callable<?>> queue = new LinkedBlockingDeque<>();
    private final Map<Object, Object> clones = Collections.synchronizedMap(new FasterIdentityHashMap<>());

    private final List<Future<?>> tasks;

    public ParallelCopyContext(TraversalAlgorithm traversalAlgorithm, CopierRegistry registry,
        ExecutorService executor, int parallelism) {
        this.traversalAlgorithm = traversalAlgorithm;
        this.registry = registry;
        int count = parallelism > 0 ?
            parallelism :
            executor instanceof ForkJoinPool ?
                ((ForkJoinPool) executor).getParallelism() :
                Runtime.getRuntime().availableProcessors();
        this.tasks = Stream.generate(() -> (Callable<?>) () -> {
                processQueue();
                return null;
            })
            .limit(count)
            .map(executor::submit)
            .collect(Collectors.toList());
    }

    private void processQueue() throws Exception {
        log("Started queue handler");
        while (true) {
            try {
//                Callable<?> callable = traversalAlgorithm.take(queue);
                Callable<?> callable = queue.take();
                log("Took task");
                callable.call();
            }
            catch (BreakException e) {
                log("Breaking queue handler");
                break;
            }
        }
    }

    @Override
    public <T> T copy(T original) throws Exception {
        if (original == null) {
            return null;
        }
        ObjectCopier<T> typeCloner = registry.getCopier(original);
        if (typeCloner instanceof TrivialCopier) {
            return ((TrivialCopier<T>) typeCloner).trivialCopy(original);
        }
        CopyResult<T> result;
        synchronized (original) {
            T clone = (T) clones.get(original);
            if (clone != null) {
                return clone;
            }
            result = typeCloner.copy(original, this);
            log("Cloned %s@%s", original.getClass().getName(), Integer.toHexString(System.identityHashCode(original)));
            clones.put(original, result.getObject());
        }
        List<Callable<?>> next = result.getNext();
        for (Callable<?> callable : next) {
            queue.put(callable);
        }
        return result.getObject();
    }

    @Override
    public void complete() throws Exception {
        while (!queue.isEmpty()) {
            log("Queue is not empty");
            for (Future<?> task : tasks) {
                if (task.isDone()) {
                    getResultOrThrowException(task);
                }
            }
            Thread.yield();
        }

        log("Queue is empty");
        for (int k = 0, count = tasks.size(); k < count; k++) {
            queue.put(BREAK);
        }

        log("Waiting for completion");
        for (Future<?> tasks : tasks) {
            getResultOrThrowException(tasks);
        }
    }

    private static <T> T getResultOrThrowException(Future<T> future) throws InterruptedException {
        try {
            return future.get();
        }
        catch (ExecutionException e) {
            throw ParallelCopyContext.<RuntimeException>uncheckedThrow(e.getCause());
        }
    }

    private static <T extends Throwable> T uncheckedThrow(Throwable t) throws T {
        throw (T) t;
    }

    private static synchronized void log(String format, Object... args) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + String.format(format, args));
    }

}
