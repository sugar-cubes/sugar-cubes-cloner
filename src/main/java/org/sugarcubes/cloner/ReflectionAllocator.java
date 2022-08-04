package org.sugarcubes.cloner;

import static org.sugarcubes.cloner.Executable.unchecked;

/**
 * Allocator which uses no-arg constructor to create object.
 *
 * @see ObjenesisAllocator
 *
 * @author Maxim Butov
 */
public class ReflectionAllocator implements ObjectAllocator {

    @Override
    public <T> T newInstance(Class<T> type) {
        return unchecked(() -> ReflectionUtils.makeAccessible(type.getDeclaredConstructor()).newInstance());
    }

}
