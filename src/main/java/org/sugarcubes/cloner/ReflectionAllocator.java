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
    public <T> T newInstance(Class<T> clazz) throws Throwable {
        return ReflectionUtils.makeAccessible(clazz.getDeclaredConstructor()).newInstance();
    }

}
