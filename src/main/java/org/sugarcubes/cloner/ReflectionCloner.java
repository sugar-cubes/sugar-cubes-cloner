package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * The implementation of {@link Cloner} which uses Java reflection API for cloning.
 *
 * @author Maxim Butov
 */
public class ReflectionCloner extends AbstractCloner {

    private ObjectAllocator allocator;
    private CloningPolicy policy;
    private Map<Class<?>, ObjectCopier<?>> cloners;

    private Map<Class<?>, ObjectCopier<?>> clonerCache;
    private FieldCache fieldCache;

    public ReflectionCloner() {
        this(null, null);
    }

    public ReflectionCloner(ObjectAllocator allocator) {
        this(allocator, null);
    }

    public ReflectionCloner(CloningPolicy policy) {
        this(null, policy);
    }

    public ReflectionCloner(ObjectAllocator allocator, CloningPolicy policy) {
        this.allocator = allocator;
        this.policy = policy;
    }

    public ObjectAllocator getAllocator() {
        if (allocator == null) {
            allocator = createAllocator();
        }
        return allocator;
    }

    public void setAllocator(ObjectAllocator allocator) {
        this.allocator = allocator;
    }

    public CloningPolicy getPolicy() {
        if (policy == null) {
            policy = new DefaultCloningPolicy();
        }
        return policy;
    }

    public void setPolicy(CloningPolicy policy) {
        this.policy = policy;
        clearCaches();
    }

    public Map<Class<?>, ObjectCopier<?>> getCloners() {
        if (cloners == null) {
            cloners = new HashMap<>();
        }
        return cloners;
    }

    public void setCloners(Map<Class<?>, ObjectCopier<?>> cloners) {
        this.cloners = cloners;
    }

    protected ObjectAllocator createAllocator() {
        return ObjenesisUtils.isObjenesisAvailable() ? new ObjenesisAllocator() : new ReflectionAllocator();
    }

    protected void clearCaches() {
        this.clonerCache = null;
        this.fieldCache = null;
    }

    @Override
    protected Object doClone(Object object) throws Throwable {
        if (clonerCache == null) {
            clonerCache = new HashMap<>();
        }
        clonerCache.putAll(getCloners());
        if (fieldCache == null) {
            fieldCache = new FieldCache(getPolicy());
        }
        CopyContextImpl context = new CopyContextImpl(type -> clonerCache.computeIfAbsent(type, this::findCloner));
        Object clone = context.copy(object);
        context.complete();
        return clone;
    }

    protected ObjectCopier<?> findCloner(Class<?> type) {
        CopyAction action = getPolicy().getTypeAction(type);
        switch (action) {
            case NULL:
                return ObjectCopier.NULL;
            case ORIGINAL:
                return ObjectCopier.NOOP;
            case DEFAULT:
                if (type.isArray()) {
                    return getPolicy().isComponentTypeImmutable(type.getComponentType()) ?
                        ObjectCopier.SHALLOW : ObjectCopier.OBJECT_ARRAY;
                }
                else {
                    return reflectionCopier;
                }
            default:
                throw new IllegalStateException();
        }
    }

    private final ReflectionObjectCopier<?> reflectionCopier = new ReflectionObjectCopier<>();;

    class ReflectionObjectCopier<T> extends TwoPhaseObjectCopier<T> {

        @Override
        public T allocate(T original) throws Throwable {
            return (T) getAllocator().newInstance(original.getClass());
        }

        @Override
        public void deepCopy(T original, T clone, CopyContext context) throws Throwable {
            for (Map.Entry<Field, CopyAction> entry : fieldCache.getFields(original.getClass())) {
                copyField(original, clone, entry.getKey(), entry.getValue(), context);
            }
        }

    }

    protected void copyField(Object original, Object clone, Field field, CopyAction action, CopyContext context) throws Throwable {
        switch (action) {
            case NULL:
                field.set(clone, null);
                break;
            case ORIGINAL:
                field.set(clone, field.get(original));
                break;
            case DEFAULT:
                field.set(clone, context.copy(field.get(original)));
                break;
            default:
                throw new IllegalStateException();
        }
    }

}
