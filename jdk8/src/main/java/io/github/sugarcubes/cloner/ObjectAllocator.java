package io.github.sugarcubes.cloner;

/**
 * Object allocator.
 *
 * @author Maxim Butov
 */
public interface ObjectAllocator<T> {

    /**
     * Creates an "empty" copy of the original object.
     *
     * @param original original object
     * @return new instance of the same type as original
     * @throws Exception if something went wrong
     */
    T allocate(T original) throws Exception;

}
