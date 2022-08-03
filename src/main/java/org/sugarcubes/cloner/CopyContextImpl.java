package org.sugarcubes.cloner;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Implementation of {@link CopyContext}.
 *
 * @author Maxim Butov
 */
public class CopyContextImpl implements CopyContext {

    private final Function<Class<?>, ObjectCopier<?>> copierFactory;
    private final SimpleQueue<UnsafeRunnable> queue;

    private final Map<Object, Object> clones = new IdentityHashMap<>();

    /**
     * Creates new instance of copying context.
     *
     * @param copierFactory method reference for obtaining copiers
     * @param traversalAlgorithm object graph traversal algorithm
     */
    public CopyContextImpl(Function<Class<?>, ObjectCopier<?>> copierFactory, GraphTraversalAlgorithm traversalAlgorithm) {
        this.copierFactory = copierFactory;
        switch (traversalAlgorithm) {
            case DEPTH_FIRST:
                queue = SimpleQueue.lifo();
                break;
            case BREADTH_FIRST:
                queue = SimpleQueue.fifo();
                break;
            default:
                throw new IllegalStateException();
        }
    }


    @Override
    public <T> T copy(T original) throws Throwable {
        if (original == null) {
            return null;
        }
        ObjectCopier<T> typeCloner = (ObjectCopier) copierFactory.apply(original.getClass());
        if (typeCloner.isTrivial()) {
            return typeCloner.copy(original, null);
        }
        T clone = (T) clones.get(original);
        if (clone != null) {
            return clone;
        }
        clone = typeCloner.copy(original, this);
        return clone;
    }

    @Override
    public <T> T register(T original, T clone) {
        Object prev = clones.put(original, clone);
        if (prev != null) {
            throw new IllegalStateException("Already registered.");
        }
        return clone;
    }

    @Override
    public void fork(UnsafeRunnable runnable) {
        queue.offer(runnable);
    }

    @Override
    public void join() throws Throwable {
        for (UnsafeRunnable next; (next = queue.poll()) != null; ) {
            next.run();
        }
    }

}
