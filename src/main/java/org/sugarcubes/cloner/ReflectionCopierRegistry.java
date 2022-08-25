package org.sugarcubes.cloner;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copier registry implementation.
 *
 * @author Maxim Butov
 */
public class ReflectionCopierRegistry implements CopierRegistry {

    /**
     * Object allocator.
     */
    private final ObjectAllocator allocator;

    /**
     * Copy policy.
     */
    private final CopyPolicy policy;

    /**
     * Field copier factory.
     */
    private final FieldCopierFactory fieldCopierFactory;

    /**
     * Cache of copiers.
     */
    private final LazyCache<Class<?>, ObjectCopier<?>> copiers = new LazyCache<>(this::findCopier);

    /**
     * Cache of reflection copiers.
     */
    private final Map<Class<?>, ReflectionCopier<?>> reflectionCopiers = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param policy copy policy
     * @param allocator object allocator
     * @param copiers predefined copiers
     * @param fieldCopierFactory field copier factory
     */
    public ReflectionCopierRegistry(CopyPolicy policy, ObjectAllocator allocator, Map<Class<?>, ObjectCopier<?>> copiers,
        FieldCopierFactory fieldCopierFactory) {
        this.policy = policy;
        this.allocator = allocator;
        this.fieldCopierFactory = fieldCopierFactory;
        this.copiers.putAll(copiers);
    }

    @Override
    public ObjectCopier<?> getCopier(Class<?> type) {
        return copiers.get(type);
    }

    /**
     * Finds or creates copier if it was not created yet.
     *
     * @param type object type
     * @return copier
     */
    protected ObjectCopier<?> findCopier(Class<?> type) {
        CopyAction action = policy.getTypeAction(type);
        switch (action) {
            case NULL:
                return ObjectCopier.NULL;
            case ORIGINAL:
                return ObjectCopier.NOOP;
            case DEFAULT:
                if (type.isArray()) {
                    Class<?> componentType = type.getComponentType();
                    if (componentType.isPrimitive() || componentType.isEnum()) {
                        return ObjectCopier.SHALLOW;
                    }
                    if (Modifier.isFinal(componentType.getModifiers()) &&
                        policy.getTypeAction(componentType) == CopyAction.ORIGINAL) {
                        return ObjectCopier.SHALLOW;
                    }
                    return ObjectCopier.OBJECT_ARRAY;
                }
                else {
                    return type.isEnum() ? ObjectCopier.NOOP : findReflectionCopier(type);
                }
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Returns {@link ReflectionCopier} instance for the type.
     *
     * @param type object type
     * @return copier instance
     */
    protected ReflectionCopier<?> findReflectionCopier(Class<?> type) {
        ReflectionCopier<?> copier = reflectionCopiers.get(type);
        if (copier == null) {
            Class<?> superType = type.getSuperclass();
            ReflectionCopier<?> parent = superType != null ? findReflectionCopier(superType) : null;
            copier = new ReflectionCopier<>(policy, allocator, type, fieldCopierFactory, parent);
            reflectionCopiers.put(type, copier);
        }
        return copier;
    }

}
