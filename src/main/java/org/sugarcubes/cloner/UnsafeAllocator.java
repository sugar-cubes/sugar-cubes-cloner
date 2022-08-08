package org.sugarcubes.cloner;

import sun.misc.Unsafe;

/**
 * Object factory which uses {@link Unsafe} to create object instance.
 *
 * @author Maxim Butov
 */
public class UnsafeAllocator implements ObjectAllocator {

    @Override
    public <T> T newInstance(Class<T> type) throws Exception {
        return (T) UnsafeUtils.getUnsafe().allocateInstance(type);
    }

}
