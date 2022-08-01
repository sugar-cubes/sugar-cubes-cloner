package org.sugarcubes.cloner;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Maxim Butov
 */
public class ReflectionCloner extends AbstractCloner {

    private ObjectAllocator allocator = new ObjenesisObjectAllocator();
    private CloningPolicy policy = new DefaultCloningPolicy();
    private Map<Class<?>, ObjectCloner<?>> cloners;

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

    protected ObjectAllocator createAllocator() {
        return ObjenesisUtils.isObjenesisAvailable() ? new ObjenesisObjectAllocator() : new ReflectionObjectAllocator();
    }

    @Override
    protected Object doClone(Object object) throws Throwable {
        ClonerContextImpl context = new ClonerContextImpl(getPolicy(), getAllocator(), getCloners());
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

}
