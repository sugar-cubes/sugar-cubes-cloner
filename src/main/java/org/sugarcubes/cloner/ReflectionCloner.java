package org.sugarcubes.cloner;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.sugarcubes.cloner.Check.argNotNull;

/**
 * Abstract base implementation of {@link Cloner} which uses Java reflection API for cloning.
 *
 * @author Maxim Butov
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class ReflectionCloner implements Cloner {

    /**
     * Object allocator.
     */
    protected final ObjectAllocator allocator;

    /**
     * Copy policy.
     */
    protected final CopyPolicy policy;

    /**
     * Field copier factory.
     */
    private final FieldCopierFactory fieldCopierFactory;

    /**
     * Cache of copiers.
     */
    private final LazyCache<Class<?>, ObjectCopier<?>> copiers = new LazyCache<>(this::getCopier);

    /**
     * Cache of reflection copiers.
     */
    private final Map<Class<?>, ReflectionCopier<?>> reflectionCopiers = new ConcurrentHashMap<>();

    /**
     * Copy context factory.
     */
    private final CopyContextFactory contextFactory;

    /**
     * Constructor.
     *
     * @param allocator object allocator
     * @param policy copy policy
     * @param fieldCopierFactory field copier factory
     * @param contextFactory context factory
     */
    public ReflectionCloner(ObjectAllocator allocator, CopyPolicy policy, Map<Class<?>, ObjectCopier<?>> copiers,
        FieldCopierFactory fieldCopierFactory, CopyContextFactory contextFactory) {
        this.allocator = argNotNull(allocator, "Allocator");
        this.policy = argNotNull(policy, "Policy");
        this.fieldCopierFactory = argNotNull(fieldCopierFactory, "Field copier factory");
        this.contextFactory = contextFactory;
        this.copiers.putAll(copiers);
    }

    @Override
    public <T> T clone(T object) {
        try {
            CompletableCopyContext context = contextFactory.newContext(copiers::get);
            T clone = context.copy(object);
            context.complete();
            return clone;
        }
        catch (ClonerException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ClonerException(e);
        }
    }

    /**
     * Chooses or creates copier for the type.
     *
     * @param type type
     * @return copier
     */
    protected ObjectCopier<?> getCopier(Class<?> type) {
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
                    return getReflectionCopier(type);
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
    protected ReflectionCopier<?> getReflectionCopier(Class<?> type) {
        ReflectionCopier<?> copier = reflectionCopiers.get(type);
        if (copier != null) {
            return copier;
        }
        Class<?> superType = type.getSuperclass();
        ReflectionCopier<?> superCopier = superType != null ? getReflectionCopier(superType) : null;
        copier = newReflectionCopier(type, superCopier);
        reflectionCopiers.put(type, copier);
        return copier;
    }

    /**
     * Creates {@link ReflectionCopier} instance for the type.
     *
     * @param type object type
     * @param superCopier copier for super type
     * @return copier instance
     */
    protected ReflectionCopier<?> newReflectionCopier(Class<?> type, ReflectionCopier<?> superCopier) {
        return new ReflectionCopier<>(allocator, policy, type, fieldCopierFactory, superCopier);
    }

}
