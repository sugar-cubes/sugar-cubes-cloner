package org.sugarcubes.cloner;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * Copier registry implementation with annotations processing.
 *
 * @author Maxim Butov
 */
public class AnnotatedCopierRegistry extends ReflectionCopierRegistry {

    /**
     * Constructor.
     *
     * @param policy copy policy
     * @param allocator object allocator
     * @param copiers predefined copiers
     * @param fieldCopierFactory field copier factory
     */
    public AnnotatedCopierRegistry(CopyPolicy policy, ObjectAllocator allocator, Map<Class<?>, ObjectCopier<?>> copiers,
        FieldCopierFactory fieldCopierFactory) {
        super(policy, allocator, copiers, fieldCopierFactory);
    }

    @Override
    protected ObjectCopier<?> findCopier(Class<?> type) {
        TypeCopier annotation = type.getDeclaredAnnotation(TypeCopier.class);
        if (annotation != null) {
            Class<ObjectCopier<?>> copierClass = annotation.value();
            Constructor<ObjectCopier<?>> constructor = ReflectionUtils.getConstructor(copierClass);
            return ReflectionUtils.execute(constructor::newInstance);
        }
        return super.findCopier(type);
    }

}
