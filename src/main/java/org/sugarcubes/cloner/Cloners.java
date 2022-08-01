package org.sugarcubes.cloner;

import org.sugarcubes.cloner.impl.ReflectionCloner;
import org.sugarcubes.cloner.impl.SerializableCloner;

/**
 * Factory for creating different {@link Cloner}s.
 *
 * {@link ReflectionCloner} is ~100 times faster than {@link SerializableCloner} and can deal with non-serializable objects.
 *
 * @author Maxim Butov
 */
public class Cloners {

    /**
     * Returns an instance of {@link ReflectionCloner} with default policy and allocator.
     *
     * @return {@link ReflectionCloner} instance
     *
     * @see ReflectionCloner#ReflectionCloner()
     */
    public static ReflectionCloner reflection() {
        return new ReflectionCloner();
    }

    /**
     * Returns an instance of {@link SerializableCloner}.
     *
     * @return {@link SerializableCloner} instance
     */
    public static Cloner serialization() {
        return SerializableCloner.INSTANCE;
    }

    /**
     * Clones object using {@link #reflection()} cloner.
     *
     * @param object original object
     *
     * @return clone
     */
    public static <T> T reflectionClone(T object) {
        return reflection().clone(object);
    }

    /**
     * Clones object using {@link #serialization()} cloner.
     *
     * @param object original object
     *
     * @return clone
     */
    public static <T> T serializationClone(T object) {
        return serialization().clone(object);
    }

}
