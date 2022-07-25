package org.sugarcubes.cloner;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

/**
 * Object factory which uses Objenesis library to create objects.
 *
 * @author Maxim Butov
 */
public class ObjenesisObjectFactory implements ClonerObjectFactory {

    private final Objenesis objenesis;

    /**
     * Default constructor.
     */
    public ObjenesisObjectFactory() {
        this(new ObjenesisStd());
    }

    /**
     * Constructor with an {@link Objenesis} instance.
     */
    public ObjenesisObjectFactory(Objenesis objenesis) {
        this.objenesis = objenesis;
    }

    @Override
    public <T> T newInstanceUnsafe(Class<T> clazz) throws Throwable {
        return objenesis.newInstance(clazz);
    }

}
