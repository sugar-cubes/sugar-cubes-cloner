package org.sugarcubes.cloner.impl;

import org.sugarcubes.cloner.ObjectAllocator;

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
