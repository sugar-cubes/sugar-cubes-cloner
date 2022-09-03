package org.sugarcubes.cloner;

import java.util.concurrent.Callable;

/**
 * Context of the single cloning process.
 *
 * @author Maxim Butov
 */
public interface CopyContext {

    /**
     * Registers clone in the context.
     *
     * @param <T> object type
     * @param original original object
     * @param clone clone
     */
    <T> void register(T original, T clone);

    /**
     * Returns the instant copy of the object. It can be a copy, saved previously into cache, or fresh copy. The copy may not be
     * the full copy of the original object, the inner state can be processed later.
     *
     * @param <T> object type
     * @param original original
     * @return clone
     * @throws Exception if something went wrong
     */
    <T> T copy(T original) throws Exception;

    /**
     * Invokes task. It can be invoked immediately or later depending on the context implementation.
     *
     * @param task task
     * @throws Exception if something went wrong
     */
    void thenInvoke(Callable<?> task) throws Exception;

}
