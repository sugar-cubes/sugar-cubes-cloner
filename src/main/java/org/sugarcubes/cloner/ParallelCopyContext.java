package org.sugarcubes.cloner;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Maxim Butov
 */
public final class ParallelCopyContext<V> extends RecursiveCallableTask<V> implements CopyContext {

    private final CopierRegistry registry;
    private final V root;
    private final Map<Object, Object> clones = Collections.synchronizedMap(new FasterIdentityHashMap<>());
    private final Deque<ForkJoinTask<?>> queue = new LinkedBlockingDeque<>(32);

    public ParallelCopyContext(CopierRegistry registry, V root) {
        this.registry = registry;
        this.root = root;
    }

    @Override
    protected V call() throws Exception {
        return copy(root);
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
            clones.put(original, result.getObject());
        }
        List<Callable<?>> next = result.getNext();
        next.stream()
            .map(RecursiveCallableTask::of)
            .map(ForkJoinTask::fork)
            .forEach(queue::offerLast);
        return result.getObject();
    }

    @Override
    public void complete() {
        for (ForkJoinTask<?> next; (next = queue.pollFirst()) != null; ) {
            next.join();
        }
        join();
    }

}
