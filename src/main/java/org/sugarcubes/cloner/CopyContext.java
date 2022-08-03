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
    void fork(UnsafeRunnable runnable);

    /**
     * Returns the instant copy of the object. It can be a copy, saved previously into cache, or fresh copy.
     *
     * @param <T> object type
     * @param original original
     * @return clone
     * @throws Throwable if something went wrong
     */
    <T> T copy(T original) throws Throwable;

    /**
     * Registers object as cloned.
     *
     * @param <T> object type
     * @param original original
     * @param clone clone
     */
    <T> void register(T original, T clone);

    /**
     * Completes all the delayed tasks.
     *
     * @throws Throwable if something went wrong
     */
    void join() throws Throwable;

}
