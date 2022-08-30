package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Copier which creates object with {@link #factory}, when copying, enumerates fields,
 * filters by policy and copies via reflection.
 *
 * @author Maxim Butov
 */
public class ReflectionCopier<T> extends TwoPhaseObjectCopier<T> {

    private final Class<T> type;
    private final ObjectFactory<T> factory;
    private final ReflectionCopier<? super T> parent;
    private final FieldCopier[] fieldCopiers;

    static /* record */ class FieldAndAction {

        final Field field;
        final FieldCopyAction action;

        FieldAndAction(Field field, FieldCopyAction action) {
            this.field = field;
            this.action = action;
        }
    }

    /**
     * Creates reflection copier.
     *
     * @param policy copy policy
     * @param allocator object allocator
     * @param type object type
     * @param fieldCopierFactory field copier factory
     * @param parent copier for the super type
     */
    @SuppressWarnings("unchecked")
    public ReflectionCopier(CopyPolicy policy, ObjectAllocator allocator, Class<T> type, FieldCopierFactory fieldCopierFactory,
        ReflectionCopier<?> parent) {
        this.type = type;
        this.factory = allocator.getFactory(type);
        this.parent = (ReflectionCopier<? super T>) (parent != null && parent.fieldCopiers.length == 0 ?
            parent.parent : parent);
        this.fieldCopiers = Arrays.stream(type.getDeclaredFields())
            .filter(ReflectionUtils::isNonStatic)
            .map(field -> new FieldAndAction(field, policy.getFieldAction(field)))
            .filter(fa -> fa.action != FieldCopyAction.SKIP)
            .map(fa -> fieldCopierFactory.getFieldCopier(fa.field, fa.action))
            .toArray(FieldCopier[]::new);
    }

    @Override
    public T allocate(T original) throws Exception {
        return factory.newInstance();
    }

    @Override
    public void deepCopy(T original, T clone, CopyContext context) throws Exception {
        if (parent != null) {
            parent.deepCopy(original, clone, context);
        }
        for (FieldCopier field : fieldCopiers) {
            field.copy(original, clone, context);
        }
    }

}
