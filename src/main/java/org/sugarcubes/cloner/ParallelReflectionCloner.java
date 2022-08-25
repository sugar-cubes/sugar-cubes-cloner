package org.sugarcubes.cloner;

import java.util.concurrent.ExecutorService;

/**
 * Parallel reflection cloner.
 *
 * @author Maxim Butov
 */
public class ParallelReflectionCloner extends AbstractReflectionCloner {

    private final CopierRegistry registry;
    private final ExecutorService executor;

    /**
     * Creates cloner instance.
     *
     * @param registry copier registry
     * @param executor executor service
     */
    public ParallelReflectionCloner(CopierRegistry registry, ExecutorService executor) {
        this.registry = registry;
        this.executor = executor;
    }

    @Override
    protected CopyContext newCopyContext() {
        return new ParallelCopyContext(registry, executor);
    }

}
