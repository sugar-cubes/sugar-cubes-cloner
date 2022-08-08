package org.sugarcubes.cloner;

/**
 * Allocator which uses no-arg constructor to create object.
 *
 * @see ObjenesisAllocator
 *
 * @author Maxim Butov
 */
public class ReflectionAllocator implements ObjectAllocator {

    @Override
    public <T> T newInstance(Class<T> type) throws Exception {
        return getFactory(type).newInstance();
    }

    @Override
    public <T> ObjectFactory<T> getFactory(Class<T> type) {
        return ReflectionUtils.getConstructor(type)::newInstance;
    }

}
