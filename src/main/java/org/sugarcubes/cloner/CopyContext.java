package org.sugarcubes.cloner;

/**
 * Context of the single cloning process.
 *
 * @author Maxim Butov
 */
public interface CopyContext {

    /**
     * No-op copy context for tests.
     */
    CopyContext NOOP = new CopyContext() {
        @Override
        public <T> T copy(T original) throws Exception {
            return original;
        }
    };

    /**
     * Returns the instant copy of the object. It can be a copy, saved previously into cache, or fresh copy.
     *
     * @param <T> object type
     * @param original original
     * @return clone
     * @throws Exception if something went wrong
     */
    <T> T copy(T original) throws Exception;

}
