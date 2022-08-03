package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.sugarcubes.cloner.Check.argNotNull;
import static org.sugarcubes.cloner.Check.isNull;

/**
 * The implementation of {@link Cloner} which uses Java reflection API for cloning.
 *
 * @author Maxim Butov
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class ReflectionCloner extends AbstractCloner {

    private static final Map<Class<?>, ObjectCopier<?>> DEFAULT_COPIERS;

    static {
        HashMap<Class<?>, ObjectCopier<?>> defaultCopiers = new HashMap<>();

        defaultCopiers.put(java.util.Date.class, ObjectCopier.SHALLOW);
        defaultCopiers.put(java.util.GregorianCalendar.class, ObjectCopier.SHALLOW);
        defaultCopiers.put(ReflectionUtils.classForName("java.util.RegularEnumSet"), ObjectCopier.SHALLOW);
        defaultCopiers.put(ReflectionUtils.classForName("java.util.JumboEnumSet"), ObjectCopier.SHALLOW);

        DEFAULT_COPIERS = Collections.unmodifiableMap(defaultCopiers);
    }

    private final ObjectAllocator allocator;
    private final CloningPolicy policy;

    private GraphTraversalAlgorithm traversalAlgorithm = GraphTraversalAlgorithm.DEPTH_FIRST;

    private final LazyCache<Class<?>, ObjectCopier<?>> copiers;

    private final ClassFieldCache fieldCache = new ClassFieldCache();

    private final LazyCache<Class<?>, CopyAction> typeActions;
    private final LazyCache<Field, CopyAction> fieldActions;

    /**
     * Constructor.
     */
    public ReflectionCloner() {
        this(ObjectAllocator.defaultAllocator(), CloningPolicy.DEFAULT);
    }

    /**
     * Constructor.
     *
     * @param allocator object allocator
     */
    public ReflectionCloner(ObjectAllocator allocator) {
        this(allocator, CloningPolicy.DEFAULT);
    }

    /**
     * Constructor.
     *
     * @param policy cloning policy
     */
    public ReflectionCloner(CloningPolicy policy) {
        this(ObjectAllocator.defaultAllocator(), policy);
    }

    /**
     * Constructor.
     *
     * @param allocator object allocator
     * @param policy cloning policy
     */
    public ReflectionCloner(ObjectAllocator allocator, CloningPolicy policy) {
        this.allocator = argNotNull(allocator, "Allocator");
        this.policy = argNotNull(policy, "Policy");
        this.typeActions = new LazyCache<>(policy::getTypeAction);
        this.fieldActions = new LazyCache<>(policy::getFieldAction);
        this.copiers = new LazyCache<>(this::getCopier);
        this.copiers.putAll(DEFAULT_COPIERS);
    }

    /**
     * Registers custom copier for type.
     *
     * @param <T> object type
     * @param type object type
     * @param copier custom copier
     * @return same cloner instance
     */
    public <T> ReflectionCloner copier(Class<T> type, ObjectCopier<T> copier) {
        argNotNull(type, "Type");
        argNotNull(copier, "Copier");
        isNull(copiers.put(type, copier), "Copier for %s already set.", type.getName());
        return this;
    }

    /**
     * Registers custom action for type.
     *
     * @param type object type
     * @param action custom action
     * @return same cloner instance
     */
    public ReflectionCloner type(Class<?> type, CopyAction action) {
        argNotNull(type, "Type");
        argNotNull(action, "Action");
        isNull(typeActions.put(type, action), "Action for %s already set.", type.getName());
        return this;
    }

    /**
     * Registers custom action for field.
     *
     * @param field field
     * @param action custom action
     * @return same cloner instance
     */
    public ReflectionCloner field(Field field, CopyAction action) {
        argNotNull(field, "Field");
        argNotNull(action, "Action");
        isNull(fieldActions.put(field, action), "Action for %s already set.", field);
        return this;
    }

    /**
     * Registers custom action for field.
     *
     * @param type object type
     * @param field declared field name
     * @param action custom action
     * @return same cloner instance
     */
    public ReflectionCloner field(Class<?> type, String field, CopyAction action) {
        argNotNull(type, "Type");
        argNotNull(field, "Field");
        argNotNull(action, "Action");
        return field(ReflectionUtils.execute(() -> type.getDeclaredField(field)), action);
    }

    /**
     * Sets traversal algorithm for objects graph.
     *
     * @param traversalAlgorithm traversal algorithm
     * @return same cloner instance
     */
    public ReflectionCloner traversalAlgorithm(GraphTraversalAlgorithm traversalAlgorithm) {
        this.traversalAlgorithm = argNotNull(traversalAlgorithm, "Traversal algorithm");
        return this;
    }

    @Override
    protected Object doClone(Object object) throws Throwable {
        CopyContext context = new CopyContextImpl(copiers::get, traversalAlgorithm);
        Object clone = context.copy(object);
        context.join();
        return clone;
    }

    /**
     * Creates copier for the type.
     *
     * @param type type
     * @return copier
     */
    protected ObjectCopier<?> getCopier(Class<?> type) {
        CopyAction action = typeActions.get(type);
        switch (action) {
            case NULL:
                return ObjectCopier.NULL;
            case ORIGINAL:
                return ObjectCopier.NOOP;
            case DEFAULT:
                if (type.isArray()) {
                    return CloningPolicyHelper.isComponentTypeImmutable(policy, type.getComponentType()) ?
                        ObjectCopier.SHALLOW : ObjectCopier.OBJECT_ARRAY;
                }
                else {
                    return reflectionCopier;
                }
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Default copier for objects.
     */
    private final ReflectionObjectCopier<?> reflectionCopier = new ReflectionObjectCopier<>();

    /**
     * Copier which creates object with {@link ReflectionCloner#allocator}, when copying, enumerates fields,
     * filters by policy and copies via reflection.
     */
    private class ReflectionObjectCopier<T> extends TwoPhaseObjectCopier<T> {

        @Override
        public T allocate(T original) throws Throwable {
            return (T) allocator.newInstance(original.getClass());
        }

        @Override
        public void deepCopy(T original, T clone, CopyContext context) throws Throwable {
            for (Field field : fieldCache.get(original.getClass())) {
                copyField(original, clone, field, fieldActions.get(field), context);
            }
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
     * @throws Throwable if something went wrong
     */
    protected void copyField(Object original, Object clone, Field field, CopyAction action, CopyContext context)
        throws Throwable {
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
