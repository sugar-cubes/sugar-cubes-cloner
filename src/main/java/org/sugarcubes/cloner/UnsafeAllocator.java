package org.sugarcubes.cloner;

import static org.sugarcubes.cloner.Executable.unchecked;

import sun.misc.Unsafe;

/**
 * Object factory which uses {@link Unsafe} to create object instance.
 *
 * @author Maxim Butov
 */
public class UnsafeAllocator implements ObjectAllocator {

    @Override
    public <T> T newInstance(Class<T> type) {
        return (T) unchecked(() -> UnsafeUtils.getUnsafe().allocateInstance(type));
    }

}
