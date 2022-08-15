package org.sugarcubes.cloner;

/**
 * Factory for {@link CopyContext}.
 *
 * @author Maxim Butov
 */
public interface CopyContextFactory {

    /**
     * Creates copy context on the basis of copier registry.
     *
     * @param registry copier registry
     * @return copy context
     */
    CompletableCopyContext newContext(CopierRegistry registry);

}
