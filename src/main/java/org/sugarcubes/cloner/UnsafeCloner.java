package org.sugarcubes.cloner;

import sun.misc.Unsafe;

/**
 * Cloner implementation which uses {@link Unsafe} to create abjects and copy fields.
 *
 * @author Maxim Butov
 */
public class UnsafeCloner extends ReflectionCloner {

    /**
     * Constructor.
     */
    public UnsafeCloner() {
        super(new UnsafeAllocator());
    }

    /**
     * Constructor.
     *
     * @param policy policy
     */
    public UnsafeCloner(CloningPolicy policy) {
        super(new UnsafeAllocator(), policy);
    }

    @Override
    protected ObjectCopier<?> newReflectionCopier(Class<?> type, ObjectCopier<?> superCopier) {
        return new UnsafeCopier<>(allocator, policy, type, (UnsafeCopier) superCopier);
    }

}
