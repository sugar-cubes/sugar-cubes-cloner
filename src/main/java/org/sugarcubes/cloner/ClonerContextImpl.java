package org.sugarcubes.cloner;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Maxim Butov
 */
class ClonerContextImpl implements ClonerContext, LaterSupport {

    final Function<Class<?>, ObjectCloner<?>> clonerFactory;

    ClonerContextImpl(Function<Class<?>, ObjectCloner<?>> clonerFactory) {
        this.clonerFactory = clonerFactory;
    }

    final Deque<UnsafeRunnable> queue = new ArrayDeque<>();
    final Map<Object, Object> clones = new IdentityHashMap<>();

    @Override
    public <T> T copy(T object) throws Throwable {
        if (object == null) {
            return null;
        }
        ObjectCloner<T> typeCloner = (ObjectCloner) clonerFactory.apply(object.getClass());
        if (typeCloner.isTrivial()) {
            return typeCloner.clone(object, this);
        }
        Object prev = clones.get(object);
        if (prev != null) {
            return (T) prev;
        }
        T clone = typeCloner.clone(object, this);
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
