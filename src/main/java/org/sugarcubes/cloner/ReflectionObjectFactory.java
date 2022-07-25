package org.sugarcubes.cloner;

/**
 * Object factory which uses no-arg constructor to create object.
 *
 * @see ObjenesisObjectFactory
 *
 * @author Maxim Butov
 */
public class ReflectionObjectFactory implements ClonerObjectFactory {

    @Override
    public <T> T newInstanceUnsafe(Class<T> clazz) throws Throwable {
        return clazz.getDeclaredConstructor().newInstance();
    }

}
