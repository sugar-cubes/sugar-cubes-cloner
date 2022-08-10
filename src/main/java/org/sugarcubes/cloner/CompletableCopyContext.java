package org.sugarcubes.cloner;

/**
 * Context of the single cloning process.
 *
 * @author Maxim Butov
 */
public interface CompletableCopyContext extends CopyContext {

    /**
     * Completes all the delayed tasks.
     *
     * @throws Exception if something went wrong
     */
    void complete() throws Exception;

}
