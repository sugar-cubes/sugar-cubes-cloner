package io.github.sugarcubes.cloner.internal;

import io.github.sugarcubes.cloner.ObjenesisUtils;
import io.github.sugarcubes.cloner.ReflectionInstanceAllocatorFactory;

/**
 * Instance allocator factory interface.
 *
 * @author Maxim Butov
 */
public interface InstanceAllocatorFactory extends TypeAllocator {

    /**
     * Creates default instance allocator factory. It will be {@link InstanceAllocatorFactory} if the Objenesis library available or
     * {@link ReflectionInstanceAllocatorFactory} otherwise.
     *
     * @return default allocator
     */
    static InstanceAllocatorFactory defaultInstance() {
        return ObjenesisUtils.isObjenesisAvailable() ? new ObjenesisInstanceAllocatorFactory() : new ReflectionInstanceAllocatorFactory();
    }

    /**
     * Creates an object factory for the type.
     *
     * @param <T> object type
     * @param type type
     * @return object factory
     */
    default <T> InstanceAllocator<T> newAllocator(Class<T> type) {
        return () -> allocateInstance(type);
    }

    @Override
    default <T> T allocateInstance(Class<T> type) throws Exception {
        return newAllocator(type).newInstance();
    }
    
}
