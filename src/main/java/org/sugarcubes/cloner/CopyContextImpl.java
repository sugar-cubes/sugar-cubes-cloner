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
    private final SimpleQueue<Executable<?>> queue;

    private final Map<Object, Object> clones = new IdentityHashMap<>();

    /**
     * Creates new instance of copying context.
     *
     * @param copierFactory method reference for obtaining copiers
     * @param queue queue for delayed tasks
     */
    public CopyContextImpl(Function<Class<?>, ObjectCopier<?>> copierFactory, SimpleQueue<Executable<?>> queue) {
        this.copierFactory = copierFactory;
        this.queue = queue;
    }

    @Override
    public <T> T copy(T original) {
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
    public <T> T register(T original, Executable<T> executable) {
        Object clone;
        synchronized (original) {
            clone = clones.computeIfAbsent(original, key -> executable.unchecked());
        }
        return (T) clone;
    }

    @Override
    public void fork(Executable<?> runnable) {
        queue.offer(runnable);
    }

    @Override
    public void join() {
        for (Executable<?> next; (next = queue.poll()) != null; ) {
            next.unchecked();
        }
    }

}
