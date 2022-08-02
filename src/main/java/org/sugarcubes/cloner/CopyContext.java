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
    void invokeLater(UnsafeRunnable runnable);

    /**
     * Returns the instant copy of the object. It can be a copy, saved previously into cache, or fresh copy.
     *
     * @param object original
     * @return clone
     * @throws Throwable if something went wrong
     */
    <T> T copy(T object) throws Throwable;

    /**
     * Completes all the delayed tasks.
     *
     * @throws Throwable if something went wrong
     */
    void complete() throws Throwable;

}
