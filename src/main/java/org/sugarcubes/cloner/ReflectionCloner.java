package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The implementation of {@link Cloner} which uses Java reflection API for cloning.
 *
 * @author Maxim Butov
 */
public class ReflectionCloner extends AbstractCloner {

    private static final Map<Class<?>, ObjectCopier<?>> DEFAULT_COPIERS;

    static {
        HashMap<Class<?>, ObjectCopier<?>> defaultCopiers = new HashMap<>();

        defaultCopiers.put(ReflectionUtils.classForName("java.util.RegularEnumSet"), ObjectCopier.SHALLOW);
        defaultCopiers.put(ReflectionUtils.classForName("java.util.JumboEnumSet"), ObjectCopier.SHALLOW);

        DEFAULT_COPIERS = Collections.unmodifiableMap(defaultCopiers);
    }

    private final ObjectAllocator allocator;
    private final CloningPolicy policy;

    private final Map<Class<?>, ObjectCopier<?>> copiers = new HashMap<>(DEFAULT_COPIERS);

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
     */
    public ReflectionCloner(ObjectAllocator allocator) {
        this(allocator, CloningPolicy.DEFAULT);
    }

    /**
     * Constructor.
     */
    public ReflectionCloner(CloningPolicy policy) {
        this(ObjectAllocator.defaultAllocator(), policy);
    }

    /**
     * Constructor.
     */
    public ReflectionCloner(ObjectAllocator allocator, CloningPolicy policy) {
        this.allocator = allocator;
        this.policy = policy;

        this.typeActions = new LazyCache<>(policy::getTypeAction);
        this.fieldActions = new LazyCache<>(policy::getFieldAction);
    }

    /**
     * Registers custom copier for type.
     *
     * @param type object type
     * @param copier custom copier
     * @return same cloner instance
     */
    public <T> ReflectionCloner copier(Class<T> type, ObjectCopier<T> copier) {
        copiers.put(type, copier);
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
        typeActions.put(type, action);
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
        fieldActions.put(field, action);
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
        fieldActions.put(ReflectionUtils.execute(() -> type.getDeclaredField(field)), action);
        return this;
    }

    @Override
    protected Object doClone(Object object) throws Throwable {
        CopyContextImpl context = new CopyContextImpl(type -> copiers.computeIfAbsent(type, this::getCopier));
        Object clone = context.copy(object);
        context.complete();
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
                    return policy.isComponentTypeImmutable(type.getComponentType()) ?
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
