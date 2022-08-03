package org.sugarcubes.cloner;

/**
 * Factory for creating different {@link Cloner}s.
 *
 * {@link ReflectionCloner} is ~100 times faster than {@link SerializationCloner} and can deal with non-serializable objects.
 *
 * @author Maxim Butov
 */
public class Cloners {

    /**
     * Returns an instance of {@link ReflectionCloner} with default policy and allocator.
     *
     * @return {@link ReflectionCloner} instance
     * @see ReflectionCloner#ReflectionCloner()
     */
    public static ReflectionCloner reflection() {
        return new ReflectionCloner();
    }

    /**
     * Returns an instance of {@link SerializationCloner}.
     *
     * @return {@link SerializationCloner} instance
     */
    public static Cloner serialization() {
        return SerializationCloner.INSTANCE;
    }

    /**
     * Clones object using {@link #reflection()} cloner.
     *
     * @param <T> object type
     * @param object original object
     * @return clone
     */
    public static <T> T reflectionClone(T object) {
        return reflection().clone(object);
    }

    /**
     * Clones object using {@link #serialization()} cloner.
     *
     * @param <T> object type
     * @param object original object
     * @return clone
     */
    public static <T> T serializationClone(T object) {
        return serialization().clone(object);
    }

}
