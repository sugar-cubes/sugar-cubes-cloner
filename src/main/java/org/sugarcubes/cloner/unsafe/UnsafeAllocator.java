package org.sugarcubes.cloner.unsafe;

import org.sugarcubes.cloner.ObjectAllocator;

import sun.misc.Unsafe;

/**
 * Object factory which uses {@link Unsafe} to create object instance.
 *
 * @author Maxim Butov
 */
public class UnsafeAllocator implements ObjectAllocator {

    @Override
    public <T> T newInstance(Class<T> clazz) throws Throwable {
        return (T) UnsafeUtils.getUnsafe().allocateInstance(clazz);
    }

}
