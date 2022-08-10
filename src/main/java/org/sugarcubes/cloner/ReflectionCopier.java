package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * Copier which creates object with {@link #factory}, when copying, enumerates fields,
 * filters by policy and copies via reflection.
 *
 * @author Maxim Butov
 */
public class ReflectionCopier<T> extends TwoPhaseObjectCopier<T> {

    private final ObjectFactory<T> factory;
    private final ReflectionCopier<? super T> superCopier;
    private final Map.Entry<Field, CopyAction>[] fields;

    /**
     * Creates reflection copier.
     *
     * @param allocator object allocator
     * @param policy cloning policy
     * @param type object type
     * @param superCopier copier for the super type
     */
    @SuppressWarnings("unchecked")
    public ReflectionCopier(ObjectAllocator allocator, CloningPolicy policy,
        Class<T> type, ReflectionCopier<? super T> superCopier) {
        this.factory = allocator.getFactory(type);
        this.superCopier = superCopier != null && superCopier.fields.length == 0 ? superCopier.superCopier : superCopier;
        this.fields = Arrays.stream(type.getDeclaredFields())
            .filter(ReflectionUtils::isNonStatic)
            .peek(ReflectionUtils::makeAccessible)
            .flatMap(field -> Collections.singletonMap(field, policy.getFieldAction(field)).entrySet().stream())
            .toArray(Map.Entry[]::new);
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
        for (Map.Entry<Field, CopyAction> entry : fields) {
            copyField(original, clone, entry.getKey(), entry.getValue(), context);
        }
    }

    /**
     * Copies field value from original object into clone.
     *
     * @param original original object
     * @param clone clone
     * @param field field to copy
     * @param action action
     * @param context copying context
     * @throws Exception if something went wrong
     */
    protected void copyField(Object original, Object clone, Field field, CopyAction action, CopyContext context)
        throws Exception {
        switch (action) {
            case NULL:
                field.set(clone, null);
                break;
            case ORIGINAL:
                field.set(clone, field.get(original));
                break;
            case DEFAULT:
                field.set(clone, context.copy(field.get(original)));
                break;
            default:
                throw new IllegalStateException();
        }
    }

}
