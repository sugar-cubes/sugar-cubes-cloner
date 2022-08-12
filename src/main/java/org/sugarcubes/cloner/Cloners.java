package org.sugarcubes.cloner;

/**
 * Factory for creating different {@link Cloner}s.
 *
 * @author Maxim Butov
 */
public class Cloners {

    /**
     * Returns a new instance of {@link ReflectionClonerBuilder}.
     *
     * @return reflection cloner builder
     */
    public static ReflectionClonerBuilder builder() {
        return new ReflectionClonerBuilder();
    }

    /**
     * Returns an instance of {@link Cloner} with default settings.
     *
     * @return reflection sequential cloner
     */
    public static Cloner reflection() {
        return builder().build();
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

    /**
     * Utility class.
     */
    private Cloners() {
    }

}
