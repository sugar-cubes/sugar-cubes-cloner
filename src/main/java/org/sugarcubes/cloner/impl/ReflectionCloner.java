package org.sugarcubes.cloner.impl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.sugarcubes.cloner.ClonerException;
import org.sugarcubes.cloner.CloningPolicy;
import org.sugarcubes.cloner.CopyAction;
import org.sugarcubes.cloner.DefaultCloningPolicy;
import org.sugarcubes.cloner.ObjectAllocator;

/**
 * @author Maxim Butov
 */
public class ReflectionCloner extends AbstractCloner {

    private ObjectAllocator allocator = new ObjenesisObjectAllocator();
    private CloningPolicy policy = new DefaultCloningPolicy();
    private Map<Class<?>, ObjectCopier<?>> cloners;
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

    public Map<Class<?>, ObjectCopier<?>> getCloners() {
        if (cloners == null) {
            cloners = new HashMap<>();
        }
        return cloners;
    }

    public void setCloners(Map<Class<?>, ObjectCopier<?>> cloners) {
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
        Map<Class<?>, ObjectCopier<?>> cloners = getCloners();
        CopyContextImpl context = new CopyContextImpl(type -> cloners.computeIfAbsent(type, this::findCloner));
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

    protected ObjectCopier<?> findCloner(Class<?> type) {
        CopyAction action = getPolicy().getTypeAction(type);
        switch (action) {
            case NULL:
                return ObjectCopier.NULL;
            case SKIP:
                throw new SkipObject();
            case ORIGINAL:
                return ObjectCopier.NOOP;
            case DEFAULT:
                if (type.isArray()) {
                    return getPolicy().isComponentTypeImmutable(type.getComponentType()) ? ObjectCopier.SHALLOW : ObjectCopier.OBJECT_ARRAY;
                }
                else {
                    return new ReflectionObjectCopier<>();
                }
            default:
                throw new IllegalStateException();
        }
    }

    class ReflectionObjectCopier<T> extends TwoPhaseObjectCopier<T> {

        @Override
        public T allocate(T original) throws Throwable {
            return (T) getAllocator().newInstance(original.getClass());
        }

        @Override
        public void deepCopy(T original, T clone, CopyContext context) throws Throwable {
            for (Map.Entry<Field, CopyAction> entry : getFieldCache().getFields(original.getClass())) {
                copyField(original, clone, entry.getKey(), entry.getValue(), context);
            }
        }

    }

    protected void copyField(Object original, Object clone, Field field, CopyAction action, CopyContext context) throws Throwable {
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
