package org.sugarcubes.cloner;

import java.util.Map;

import static org.sugarcubes.cloner.Check.argNotNull;

/**
 * Sequential reflection cloner.
 *
 * @author Maxim Butov
 */
public class SequentialReflectionCloner extends AbstractReflectionCloner {

    /**
     * Object graph traversal algorithm.
     */
    private final TraversalAlgorithm traversalAlgorithm;

    /**
     * Constructor.
     *
     * @param allocator object allocator
     * @param policy cloning policy
     * @param fieldCopierFactory field copier factory
     * @param traversalAlgorithm traversal algorithm
     */
    public SequentialReflectionCloner(ObjectAllocator allocator, CloningPolicy policy, Map<Class<?>, ObjectCopier<?>> copiers,
        FieldCopierFactory fieldCopierFactory, TraversalAlgorithm traversalAlgorithm) {
        super(allocator, policy, copiers, fieldCopierFactory);
        this.traversalAlgorithm = argNotNull(traversalAlgorithm, "Traversal algorithm");
    }

    @Override
    protected CompletableCopyContext newCopyContext(CopierRegistry registry) {
        return new SequentialCopyContext(registry, traversalAlgorithm);
    }

}
