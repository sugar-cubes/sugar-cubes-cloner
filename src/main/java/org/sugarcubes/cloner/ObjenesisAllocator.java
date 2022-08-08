package org.sugarcubes.cloner;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

/**
 * Allocator which uses Objenesis library to create objects.
 *
 * @author Maxim Butov
 */
public class ObjenesisAllocator implements ObjectAllocator {

    /**
     * Objenesis instance.
     */
    private final Objenesis objenesis;

    /**
     * Default constructor.
     */
    public ObjenesisAllocator() {
        this(new ObjenesisStd());
    }

    /**
     * Constructor with an {@link Objenesis} instance.
     *
     * @param objenesis {@link Objenesis} instance
     */
    public ObjenesisAllocator(Objenesis objenesis) {
        this.objenesis = objenesis;
    }

    @Override
    public <T> T newInstance(Class<T> type) {
        return objenesis.newInstance(type);
    }

    @Override
    public <T> ObjectFactory<T> getFactory(Class<T> type) {
        return objenesis.getInstantiatorOf(type)::newInstance;
    }

}
