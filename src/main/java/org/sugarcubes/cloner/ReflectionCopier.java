package org.sugarcubes.cloner;

import java.util.Arrays;

/**
 * Copier which creates object with {@link #factory}, when copying, enumerates fields,
 * filters by policy and copies via reflection.
 *
 * @author Maxim Butov
 */
public class ReflectionCopier<T> implements TwoPhaseObjectCopier<T> {

    private final ObjectFactory<T> factory;
    private final ReflectionCopier<? super T> superCopier;
    private final FieldCopier[] fieldCopiers;

    /**
     * Creates reflection copier.
     *
     * @param allocator object allocator
     * @param policy cloning policy
     * @param type object type
     * @param fieldCopierFactory field copier factory
     * @param superCopier copier for the super type
     */
    @SuppressWarnings("unchecked")
    public ReflectionCopier(ObjectAllocator allocator, CloningPolicy policy,
        Class<T> type, FieldCopierFactory fieldCopierFactory, ReflectionCopier<?> superCopier) {
        this.factory = allocator.getFactory(type);
        this.superCopier = (ReflectionCopier<? super T>) (superCopier != null && superCopier.fieldCopiers.length == 0 ?
            superCopier.superCopier : superCopier);
        this.fieldCopiers = Arrays.stream(type.getDeclaredFields())
            .filter(ReflectionUtils::isNonStatic)
            .peek(ReflectionUtils::makeAccessible)
            .map(field -> fieldCopierFactory.getFieldCopier(field, policy))
            .toArray(FieldCopier[]::new);
    }

    @Override
    public T allocate(T original) throws Exception {
        return factory.newInstance();
    }

    @Override
    public void deepCopy(T original, T clone, CopyContext context) throws Exception {
        if (superCopier != null) {
            superCopier.deepCopy(original, clone, context);
        }
        for (FieldCopier field : fieldCopiers) {
            field.copy(original, clone, context);
        }
    }

}
