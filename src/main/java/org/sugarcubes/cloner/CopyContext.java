package org.sugarcubes.cloner;

import java.util.concurrent.Callable;

/**
 * Context of the single cloning process.
 *
 * @author Maxim Butov
 */
public interface CopyContext {

    /**
     * Returns the instant copy of the object. It can be a copy, saved previously into cache, or fresh copy.
     *
     * @param <T> object type
     * @param original original
     * @return clone
     * @throws Exception if something went wrong
     */
    <T> T copy(T original) throws Exception;

    /**
     * Registers clone in the context.
     *
     * @param <T> object type
     * @param original original object
     * @param clone clone
     */
    <T> void register(T original, T clone);

    /**
     * Saves task to be invoked later.
     *
     * @param task task
     */
    void invokeLater(Callable<?> task);

}
