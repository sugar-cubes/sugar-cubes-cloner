package org.sugarcubes.cloner.annotation;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.sugarcubes.cloner.CopyPolicy;
import org.sugarcubes.cloner.FieldCopierFactory;
import org.sugarcubes.cloner.ObjectAllocator;
import org.sugarcubes.cloner.ObjectCopier;
import org.sugarcubes.cloner.ReflectionCopierRegistry;
import org.sugarcubes.cloner.ReflectionUtils;

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
    public AnnotatedCopierRegistry(CopyPolicy policy, ObjectAllocator allocator, FieldCopierFactory fieldCopierFactory,
        Map<Class<?>, ObjectCopier<?>> copiers) {
        super(policy, allocator, copiers, fieldCopierFactory);
    }

    @Override
    protected ObjectCopier<?> findCopier(Class<?> type) {
        TypeCopier annotation = type.getDeclaredAnnotation(TypeCopier.class);
        if (annotation != null) {
            Constructor<ObjectCopier<?>> constructor = ReflectionUtils.getConstructor(annotation.value());
            return ReflectionUtils.execute(constructor::newInstance);
        }
        return super.findCopier(type);
    }

}
