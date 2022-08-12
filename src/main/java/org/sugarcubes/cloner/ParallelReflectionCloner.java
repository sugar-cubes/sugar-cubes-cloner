package org.sugarcubes.cloner;

import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Parallel reflection cloner.
 *
 * @author Maxim Butov
 */
public class ParallelReflectionCloner extends AbstractReflectionCloner {

    /**
     * Executor service.
     */
    private final ExecutorService executor;

    /**
     * Constructor.
     *
     * @param allocator object allocator
     * @param policy cloning policy
     * @param fieldCopierFactory field copier factory
     * @param executor executor service
     */
    public ParallelReflectionCloner(ObjectAllocator allocator, CloningPolicy policy, Map<Class<?>, ObjectCopier<?>> copiers,
        FieldCopierFactory fieldCopierFactory, ExecutorService executor) {
        super(allocator, policy, copiers, fieldCopierFactory);
        this.executor = Check.argNotNull(executor, "Executor");
    }

    @Override
    protected CompletableCopyContext newCopyContext(CopierRegistry registry) {
        return new ParallelCopyContext(registry, executor);
    }

}
