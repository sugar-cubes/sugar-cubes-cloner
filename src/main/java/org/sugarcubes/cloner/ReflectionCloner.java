package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Maxim Butov
 */
public class ReflectionCloner extends AbstractCloner {

    private ObjectAllocator allocator = new ObjenesisObjectAllocator();
    private CloningPolicy policy = new DefaultCloningPolicy();
    private Map<Class<?>, ObjectCloner<?>> cloners;
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
    }

    public Map<Class<?>, ObjectCloner<?>> getCloners() {
        if (cloners == null) {
            cloners = new HashMap<>();
        }
        return cloners;
    }

    public void setCloners(Map<Class<?>, ObjectCloner<?>> cloners) {
        this.cloners = cloners;
    }

    public FieldCache getFieldCache() {
        if (fieldCache == null) {
            fieldCache = new FieldCache(getPolicy());
        }
        return fieldCache;
    }

    public void setFieldCache(FieldCache fieldCache) {
        this.fieldCache = fieldCache;
    }

    protected ObjectAllocator createAllocator() {
        return ObjenesisUtils.isObjenesisAvailable() ? new ObjenesisObjectAllocator() : new ReflectionObjectAllocator();
    }

    @Override
    protected Object doClone(Object object) throws Throwable {
        Map<Class<?>, ObjectCloner<?>> cloners = getCloners();
        ClonerContextImpl context = new ClonerContextImpl(type -> cloners.computeIfAbsent(type, this::findCloner));
        Object clone;
        try {
            clone = context.copy(object);
        }
        catch (SkipObject e) {
            throw new ClonerException("Cannot skip root object.");
        }
        context.complete();
        return clone;
    }

    protected ObjectCloner<?> findCloner(Class<?> type) {
        CloningAction action = getPolicy().getTypeAction(type);
        switch (action) {
            case NULL:
                return ObjectCloner.NULL;
            case SKIP:
                throw new SkipObject();
            case ORIGINAL:
                return ObjectCloner.NOOP;
            case DEFAULT:
                if (type.isArray()) {
                    return getPolicy().isComponentTypeImmutable(type.getComponentType()) ? ObjectCloner.SHALLOW : ObjectCloner.OBJECT_ARRAY;
                }
                else {
                    return new ReflectionObjectCloner<>();
                }
            default:
                throw new IllegalStateException();
        }
    }

    class ReflectionObjectCloner<T> extends TwoPhaseObjectCloner<T> {

        @Override
        public T allocate(T original) throws Throwable {
            return (T) getAllocator().newInstance(original.getClass());
        }

        @Override
        public void deepCopy(T original, T clone, ClonerContext context) throws Throwable {
            for (Map.Entry<Field, CloningAction> entry : getFieldCache().getFields(original.getClass())) {
                copyField(original, clone, entry.getKey(), entry.getValue(), context);
            }
        }

    }

    protected void copyField(Object original, Object clone, Field field, CloningAction action, ClonerContext context) throws Throwable {
        switch (action) {
            case NULL:
                field.set(clone, null);
                break;
            case SKIP:
                return;
            case ORIGINAL:
                field.set(clone, field.get(original));
                break;
            case DEFAULT:
                Object originalValue = field.get(original);
                Object cloneValue;
                try {
                    cloneValue = context.copy(originalValue);
                }
                catch (SkipObject e) {
                    break;
                }
                field.set(clone, cloneValue);
                break;
            default:
                throw new IllegalStateException();
        }
    }

}
