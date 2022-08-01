package org.sugarcubes.cloner.impl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Maxim Butov
 */
class CopyContextImpl implements CopyContext {

    final Function<Class<?>, ObjectCopier<?>> clonerFactory;

    CopyContextImpl(Function<Class<?>, ObjectCopier<?>> clonerFactory) {
        this.clonerFactory = clonerFactory;
    }

    final Deque<UnsafeRunnable> queue = new ArrayDeque<>();
    final Map<Object, Object> clones = new IdentityHashMap<>();

    @Override
    public <T> T copy(T object) throws Throwable {
        if (object == null) {
            return null;
        }
        ObjectCopier<T> typeCloner = (ObjectCopier) clonerFactory.apply(object.getClass());
        if (typeCloner.isTrivial()) {
            return typeCloner.copy(object, this);
        }
        Object prev = clones.get(object);
        if (prev != null) {
            return (T) prev;
        }
        T clone = typeCloner.copy(object, this);
        clones.put(object, clone);
        return clone;
    }

    @Override
    public void invokeLater(UnsafeRunnable runnable) {
        queue.offerLast(runnable);
    }

    @Override
    public void complete() throws Throwable {
        for (UnsafeRunnable next; (next = queue.pollLast()) != null; ) {
            next.run();
        }
    }

}
