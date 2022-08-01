package org.sugarcubes.cloner;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author Maxim Butov
 */
class ClonerContextImpl implements ClonerContext, LaterSupport {

    final Deque<UnsafeRunnable> queue = new ArrayDeque<>();
    final Map<Object, Object> clones = new IdentityHashMap<>();
    final FieldCache fieldCache;

    final CloningPolicy policy;
    final ObjectAllocator allocator;
    final Map<Class<?>, ObjectCloner<?>> cloners;

    ClonerContextImpl(CloningPolicy policy, ObjectAllocator allocator, Map<Class<?>, ObjectCloner<?>> cloners) {
        this.cloners = new HashMap<>(cloners);
        this.policy = policy;
        this.allocator = allocator;
        this.fieldCache = new FieldCache(policy);
    }

    @Override
    public <T> T copy(T object) throws Throwable {
        if (object == null) {
            return null;
        }
        ObjectCloner<T> typeCloner = (ObjectCloner) cloners.computeIfAbsent(object.getClass(), type -> findCloner(type));
        if (typeCloner.isTrivial()) {
            return typeCloner.clone(object, null);
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
            next.run(this);
        }
    }

    private ObjectCloner<?> findCloner(Class<?> type) {
        CloningAction action = policy.getTypeAction(type);
        switch (action) {
            case NULL:
                return ObjectCloner.NULL;
            case SKIP:
                throw new SkipObject();
            case ORIGINAL:
                return ObjectCloner.NOOP;
            case DEFAULT:
                if (type.isArray()) {
                    return policy.isComponentTypeImmutable(type.getComponentType()) ? ObjectCloner.SHALLOW : ObjectCloner.OBJECT_ARRAY;
                }
                else {
                    return new ReflectionObjectCloner<>(type, fieldCache, policy, allocator);
                }
            default:
                throw new IllegalStateException();
        }
    }
}
