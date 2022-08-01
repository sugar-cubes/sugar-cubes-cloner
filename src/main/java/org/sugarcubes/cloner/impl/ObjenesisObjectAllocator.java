package org.sugarcubes.cloner.impl;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.sugarcubes.cloner.ObjectAllocator;

/**
 * Object factory which uses Objenesis library to create objects.
 *
 * @author Maxim Butov
 */
public class ObjenesisObjectAllocator implements ObjectAllocator {

    private final Objenesis objenesis;

    /**
     * Default constructor.
     */
    public ObjenesisObjectAllocator() {
        this(new ObjenesisStd());
    }

    /**
     * Constructor with an {@link Objenesis} instance.
     */
    public ObjenesisObjectAllocator(Objenesis objenesis) {
        this.objenesis = objenesis;
    }

    @Override
    public <T> T newInstanceUnsafe(Class<T> clazz) throws Throwable {
        return objenesis.newInstance(clazz);
    }

}
