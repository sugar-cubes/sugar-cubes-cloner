package org.sugarcubes.cloner;

/**
 * Context of the single cloning process.
 *
 * @author Maxim Butov
 */
public interface CopyContext {

    /**
     * Action which is executed within copying context.
     */
    @FunctionalInterface
    interface ContextAction {

        /**
         * Actual work.
         *
         * @throws Exception if something went wrong
         */
        void perform() throws Exception;

    }

    /**
     * Registers clone in the context.
     *
     * @param <T> object type
     * @param original original object
     * @param clone clone
     */
    <T> void register(T original, T clone);

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
     * Invokes task. It can be invoked immediately or later depending on the context implementation.
     *
     * @param task task
     */
    void thenInvoke(ContextAction task) throws Exception;

}
