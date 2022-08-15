package org.sugarcubes.cloner;

import java.util.Arrays;
import java.util.Collections;

/**
 * Copier which creates object with {@link #factory}, when copying, enumerates fields,
 * filters by policy and copies via reflection.
 *
 * @author Maxim Butov
 */
public class ReflectionCopier<T> implements TwoPhaseObjectCopier<T> {

    private final ObjectFactory<T> factory;
    private final TwoPhaseObjectCopier<? super T> superCopier;
    private final FieldCopier[] fieldCopiers;

    /**
     * Creates reflection copier.
     *
     * @param allocator object allocator
     * @param policy copy policy
     * @param type object type
     * @param fieldCopierFactory field copier factory
     * @param superCopier copier for the super type
     */
    @SuppressWarnings("unchecked")
    public ReflectionCopier(ObjectAllocator allocator, CopyPolicy policy,
        Class<T> type, FieldCopierFactory fieldCopierFactory, ReflectionCopier<?> superCopier) {
        this.factory = allocator.getFactory(type);
        this.superCopier = (ReflectionCopier<? super T>) (superCopier != null && superCopier.fieldCopiers.length == 0 ?
            superCopier.superCopier : superCopier);
        this.fieldCopiers = Arrays.stream(type.getDeclaredFields())
            .filter(ReflectionUtils::isNonStatic)
            .peek(ReflectionUtils::makeAccessible)
            .flatMap(field -> Collections.singletonMap(field, policy.getFieldAction(field)).entrySet().stream())
            .filter(entry -> entry.getValue() != FieldCopyAction.SKIP)
            .map(entry -> fieldCopierFactory.getFieldCopier(entry.getKey(), entry.getValue()))
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
