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
    protected <T> ReflectionCopier<T> newReflectionCopier(Class<T> type, ReflectionCopier<? super T> superCopier) {
        return new UnsafeCopier<>(allocator, policy, type, superCopier);
    }

}
