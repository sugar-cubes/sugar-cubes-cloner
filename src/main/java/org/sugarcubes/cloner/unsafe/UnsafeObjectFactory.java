package org.sugarcubes.cloner.unsafe;

import org.sugarcubes.cloner.ClonerObjectFactory;

import sun.misc.Unsafe;

/**
 * Object factory which uses {@link Unsafe} to create object instance.
 *
 * @author Maxim Butov
 */
public class UnsafeObjectFactory implements ClonerObjectFactory {

    @Override
    public <T> T newInstanceUnsafe(Class<T> clazz) throws Throwable {
        return (T) UnsafeUtils.getUnsafe().allocateInstance(clazz);
    }

}
