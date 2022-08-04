package org.sugarcubes.cloner;

/**
 * Context of the single cloning process.
 *
 * @author Maxim Butov
 */
public interface CopyContext {

    /**
     * Puts the runnable into queue for the subsequent invocation.
     *
     * @param runnable runnable
     */
    void fork(Executable<?> runnable);

    /**
     * Returns the instant copy of the object. It can be a copy, saved previously into cache, or fresh copy.
     *
     * @param <T> object type
     * @param original original
     * @return clone
     */
    <T> T copy(T original);

    /**
     * Registers object as cloned.
     *
     * @param <T> object type
     * @param original original
     * @param clone clone
     * @return clone
     */
    <T> T register(T original, Executable<T> clone);

    /**
     * Completes all the delayed tasks.
     */
    void join();

}
