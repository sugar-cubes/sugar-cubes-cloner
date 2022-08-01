package org.sugarcubes.cloner;

/**
 * Object factory which uses no-arg constructor to create object.
 *
 * @see ObjenesisObjectAllocator
 *
 * @author Maxim Butov
 */
public class ReflectionObjectAllocator implements ObjectAllocator {

    @Override
    public <T> T newInstanceUnsafe(Class<T> clazz) throws Throwable {
        return clazz.getDeclaredConstructor().newInstance();
    }

}
