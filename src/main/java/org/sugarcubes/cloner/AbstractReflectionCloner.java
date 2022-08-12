package org.sugarcubes.cloner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.sugarcubes.cloner.Check.argNotNull;
import static org.sugarcubes.cloner.CloningPolicyHelper.isComponentTypeImmutable;

/**
 * Abstract base implementation of {@link Cloner} which uses Java reflection API for cloning.
 *
 * @author Maxim Butov
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public abstract class AbstractReflectionCloner implements Cloner {

    private static final Map<Class<?>, ObjectCopier<?>> DEFAULT_COPIERS;

    static {
        Map<Class<?>, ObjectCopier<?>> defaultCopiers = new HashMap<>();

        defaultCopiers.put(java.util.Date.class, ObjectCopier.SHALLOW);
        defaultCopiers.put(java.util.GregorianCalendar.class, ObjectCopier.SHALLOW);
        defaultCopiers.put(java.util.BitSet.class, ObjectCopier.SHALLOW);
        defaultCopiers.put(ReflectionUtils.classForName("java.util.RegularEnumSet"), ObjectCopier.SHALLOW);
        defaultCopiers.put(ReflectionUtils.classForName("java.util.JumboEnumSet"), ObjectCopier.SHALLOW);

        defaultCopiers.put(java.util.ArrayDeque.class, new SimpleCollectionCopier<>(java.util.ArrayDeque::new));
        defaultCopiers.put(java.util.ArrayList.class, new SimpleCollectionCopier<>(java.util.ArrayList::new));
        defaultCopiers.put(java.util.LinkedList.class, new SimpleCollectionCopier<>(size -> new java.util.LinkedList<>()));
        defaultCopiers.put(java.util.Stack.class, new SimpleCollectionCopier<>(size -> new java.util.Stack<>()));
        defaultCopiers.put(java.util.Vector.class, new SimpleCollectionCopier<>(java.util.Vector::new));

        defaultCopiers.put(java.util.IdentityHashMap.class, new IdentityHashMapCopier());
        defaultCopiers.put(java.util.EnumMap.class, new EnumMapCopier<>());

        DEFAULT_COPIERS = Collections.unmodifiableMap(defaultCopiers);
    }

    /**
     * Object allocator.
     */
    protected final ObjectAllocator allocator;

    /**
     * Cloning policy.
     */
    protected final CloningPolicy policy;

    /**
     * Field copier factory.
     */
    private final FieldCopierFactory fieldCopierFactory;

    /**
     * Cache of copiers.
     */
    private final LazyCache<Class<?>, ObjectCopier<?>> copiers = new LazyCache<>(DEFAULT_COPIERS, this::getCopier);

    /**
     * Cache of reflection copiers.
     */
    private final Map<Class<?>, ReflectionCopier<?>> reflectionCopiers = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param allocator object allocator
     * @param policy cloning policy
     * @param fieldCopierFactory field copier factory
     */
    public AbstractReflectionCloner(ObjectAllocator allocator, CloningPolicy policy, Map<Class<?>, ObjectCopier<?>> copiers,
        FieldCopierFactory fieldCopierFactory) {
        this.allocator = argNotNull(allocator, "Allocator");
        this.policy = argNotNull(policy, "Policy");
        this.fieldCopierFactory = argNotNull(fieldCopierFactory, "Field copier factory");
        this.copiers.putAll(copiers);
    }

    /**
     * Registers custom copier for type.
     *
     * @param <T> object type
     * @param type object type
     * @param copier custom copier
     * @return same cloner instance
     */
    public <T> AbstractReflectionCloner copier(Class<T> type, ObjectCopier<T> copier) {
        argNotNull(type, "Type");
        argNotNull(copier, "Copier");
        // one can replace default copiers only
        if (copiers.put(type, copier) != DEFAULT_COPIERS.get(type)) {
            throw new IllegalArgumentException(String.format("Copier for %s already set.", type.getName()));
        }
        return this;
    }

    @Override
    public <T> T clone(T object) {
        try {
            CompletableCopyContext context = newCopyContext(copiers::get);
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
     * Create custom copy context.
     *
     * @param registry copiers registry
     * @return copy context
     */
    protected abstract CompletableCopyContext newCopyContext(CopierRegistry registry);

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
                    return isComponentTypeImmutable(policy, type.getComponentType()) ?
                        ObjectCopier.SHALLOW : ObjectCopier.OBJECT_ARRAY;
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
