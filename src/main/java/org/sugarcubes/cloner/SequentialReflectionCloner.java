package org.sugarcubes.cloner;

/**
 * Sequential reflection cloner.
 *
 * @author Maxim Butov
 */
public class SequentialReflectionCloner extends AbstractReflectionCloner {

    private final CopierRegistry registry;
    private final TraversalAlgorithm traversalAlgorithm;

    /**
     * Creates cloner instance.
     *
     * @param registry copier registry
     * @param traversalAlgorithm object graph traversal algorithm
     */
    public SequentialReflectionCloner(CopierRegistry registry, TraversalAlgorithm traversalAlgorithm) {
        this.registry = registry;
        this.traversalAlgorithm = traversalAlgorithm;
    }

    @Override
    protected CopyContext newCopyContext() {
        return new SequentialCopyContext(registry, traversalAlgorithm);
    }

}
