package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

/**
 * Builder for reflection cloners.
 *
 * @author Maxim Butov
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public final class ReflectionClonerBuilder {

    /**
     * JDK immutable types.
     */
    private static final Set<Class<?>> IMMUTABLE_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        java.math.BigDecimal.class, java.math.BigInteger.class, Boolean.class, Byte.class,
        Character.class, Class.class,
        Double.class, java.time.Duration.class,
        Float.class,
        java.net.Inet4Address.class, java.net.Inet6Address.class, java.net.InetSocketAddress.class, java.time.Instant.class,
        Integer.class,
        java.time.LocalDate.class, java.time.LocalDateTime.class, java.time.LocalTime.class, Long.class,
        java.time.MonthDay.class,
        java.net.NetworkInterface.class,
        java.time.OffsetDateTime.class, java.time.OffsetTime.class,
        java.util.regex.Pattern.class, java.time.Period.class,
        Short.class, String.class,
        java.net.URI.class, java.net.URL.class, java.util.UUID.class,
        java.time.Year.class, java.time.YearMonth.class,
        java.time.ZonedDateTime.class, java.time.ZoneOffset.class
    )));

    /**
     * Default copiers for well-known JDK types.
     */
    private static final Map<Class<?>, ObjectCopier<?>> DEFAULT_COPIERS;

    static {
        Map<Class<?>, ObjectCopier<?>> defaultCopiers = new HashMap<>();

        IMMUTABLE_TYPES.forEach(type -> defaultCopiers.put(type, ObjectCopier.NOOP));

        defaultCopiers.put(java.util.BitSet.class, ObjectCopier.SHALLOW);
        defaultCopiers.put(java.util.Date.class, ObjectCopier.SHALLOW);
        defaultCopiers.put(java.util.GregorianCalendar.class, ObjectCopier.SHALLOW);
        defaultCopiers.put(ReflectionUtils.classForName("java.util.JumboEnumSet"), ObjectCopier.SHALLOW);
        defaultCopiers.put(ReflectionUtils.classForName("java.util.RegularEnumSet"), ObjectCopier.SHALLOW);

        defaultCopiers.put(java.util.ArrayDeque.class, new SimpleCollectionCopier<>(java.util.ArrayDeque::new));
        defaultCopiers.put(java.util.ArrayList.class, new SimpleCollectionCopier<>(java.util.ArrayList::new));
        defaultCopiers.put(java.util.LinkedList.class, new SimpleCollectionCopier<>(size -> new java.util.LinkedList<>()));
        defaultCopiers.put(java.util.Stack.class, new SimpleCollectionCopier<>(size -> new java.util.Stack<>()));
        defaultCopiers.put(java.util.Vector.class, new SimpleCollectionCopier<>(java.util.Vector::new));

        defaultCopiers.put(java.util.EnumMap.class, new EnumMapCopier<>());
        defaultCopiers.put(java.util.IdentityHashMap.class, new IdentityHashMapCopier());

        DEFAULT_COPIERS = Collections.unmodifiableMap(defaultCopiers);
    }

    /**
     * Object allocator.
     */
    private ObjectAllocator allocator;

    /**
     * Field copier factory.
     */
    private FieldCopierFactory fieldCopierFactory;

    /**
     * Cloning mode.
     */
    private CloningMode mode;

    /**
     * Traversal algorithm for sequential mode.
     */
    private TraversalAlgorithm traversalAlgorithm;

    /**
     * Executor service for parallel mode.
     */
    private ExecutorService executor;

    /**
     * Custom object policy.
     */
    private ObjectPolicy objectPolicy;

    /**
     * Custom actions for objects.
     */
    private final Map<Object, CopyAction> objectActions = new IdentityHashMap<>();

    /**
     * Custom copy policy.
     */
    private CopyPolicy policy;

    /**
     * Custom actions for types.
     */
    private final Map<Class<?>, CopyAction> typeActions = new HashMap<>();

    /**
     * Custom actions for fields.
     */
    private final Map<Field, FieldCopyAction> fieldActions = new HashMap<>();

    /**
     * Custom copiers for types.
     */
    private final Map<Class<?>, ObjectCopier<?>> copiers = new HashMap<>(DEFAULT_COPIERS);

    /**
     * Creates a builder.
     */
    public ReflectionClonerBuilder() {
    }

    /**
     * Sets object allocator.
     *
     * @param allocator object allocator
     * @return same builder instance
     */
    public ReflectionClonerBuilder setAllocator(ObjectAllocator allocator) {
        Check.argNotNull(allocator, "Allocator");
        Check.isNull(this.allocator, "Allocator already set.");
        this.allocator = allocator;
        return this;
    }

    /**
     * Sets field copier factory.
     *
     * @param fieldCopierFactory field copier factory
     * @return same builder instance
     */
    public ReflectionClonerBuilder setFieldCopierFactory(FieldCopierFactory fieldCopierFactory) {
        Check.argNotNull(fieldCopierFactory, "Field copier factory");
        Check.isNull(this.fieldCopierFactory, "Field copier factory already set.");
        this.fieldCopierFactory = fieldCopierFactory;
        return this;
    }

    /**
     * Sets allocator and field copier factory which use {@link sun.misc.Unsafe}.
     *
     * @return same builder instance
     */
    public ReflectionClonerBuilder setUnsafe() {
        return setAllocator(new UnsafeAllocator()).setFieldCopierFactory(new UnsafeFieldCopierFactory());
    }

    /**
     * Sets cloning mode.
     *
     * @param mode cloning mode
     * @return same builder instance
     */
    public ReflectionClonerBuilder setMode(CloningMode mode) {
        Check.argNotNull(mode, "Mode");
        Check.isNull(this.mode, "Mode already set.");
        this.mode = mode;
        return this;
    }

    /**
     * Sets traversal algorithm for objects graph.
     *
     * @param traversalAlgorithm traversal algorithm
     * @return same builder instance
     */
    public ReflectionClonerBuilder setTraversalAlgorithm(TraversalAlgorithm traversalAlgorithm) {
        Check.argNotNull(traversalAlgorithm, "Traversal algorithm");
        Check.isNull(this.traversalAlgorithm, "Traversal algorithm already set.");
        this.traversalAlgorithm = traversalAlgorithm;
        return this;
    }

    /**
     * Enables parallel mode with given executor service.
     *
     * @param executor executor service
     * @return same builder instance
     */
    public ReflectionClonerBuilder setExecutor(ExecutorService executor) {
        Check.argNotNull(executor, "Executor");
        Check.isNull(this.executor, "Executor already set.");
        this.executor = executor;
        return this;
    }

    /**
     * Sets custom object policy.
     *
     * @param objectPolicy object policy
     * @return same builder instance
     */
    public ReflectionClonerBuilder setObjectPolicy(ObjectPolicy objectPolicy) {
        Check.argNotNull(objectPolicy, "Object policy");
        Check.isNull(this.objectPolicy, "Object policy already set.");
        this.objectPolicy = objectPolicy;
        return this;
    }

    /**
     * Registers custom action for object.
     *
     * @param original original object
     * @param action custom action
     * @return same builder instance
     */
    public ReflectionClonerBuilder setObjectAction(Object original, CopyAction action) {
        Check.argNotNull(original, "Original");
        Check.argNotNull(action, "Action");
        Check.illegalArg(objectActions.containsKey(original), "Action for %s already set.", original);
        objectActions.put(original, action);
        return this;
    }

    /**
     * Sets custom copy policy.
     *
     * @param policy copy policy
     * @return same builder instance
     */
    public ReflectionClonerBuilder setPolicy(CopyPolicy policy) {
        Check.argNotNull(policy, "Policy");
        Check.isNull(this.policy, "Policy already set.");
        this.policy = policy;
        return this;
    }

    /**
     * Registers custom action for type.
     *
     * @param type object type
     * @param action custom action
     * @return same builder instance
     */
    public ReflectionClonerBuilder setTypeAction(Class<?> type, CopyAction action) {
        Check.argNotNull(type, "Type");
        Check.argNotNull(action, "Action");
        Check.illegalArg(typeActions.containsKey(type), "Action for %s already set.", type);
        typeActions.put(type, action);
        if (action != CopyAction.DEFAULT) {
            copiers.remove(type);
        }
        return this;
    }

    /**
     * Registers custom action for type.
     *
     * @param type object type name
     * @param action custom action
     * @return same builder instance
     */
    public ReflectionClonerBuilder setTypeAction(String type, CopyAction action) {
        Check.argNotNull(type, "Type");
        Check.argNotNull(action, "Action");
        return setTypeAction(ReflectionUtils.classForName(type), action);
    }

    /**
     * Registers custom action for field.
     *
     * @param field field
     * @param action custom action
     * @return same builder instance
     */
    public ReflectionClonerBuilder setFieldAction(Field field, FieldCopyAction action) {
        Check.argNotNull(field, "Field");
        Check.argNotNull(action, "Action");
        Check.illegalArg(field.getType().isPrimitive() && action == FieldCopyAction.NULL,
            "Cannot apply action NULL for primitive field %s.", field);
        Check.illegalArg(fieldActions.containsKey(field), "Action for %s already set.", field);
        fieldActions.put(field, action);
        return this;
    }

    /**
     * Registers custom action for field.
     *
     * @param type object type
     * @param field declared field name
     * @param action custom action
     * @return same builder instance
     */
    public ReflectionClonerBuilder setFieldAction(Class<?> type, String field, FieldCopyAction action) {
        Check.argNotNull(type, "Type");
        Check.argNotNull(field, "Field");
        Check.argNotNull(action, "Action");
        return setFieldAction(ReflectionUtils.getField(type, field), action);
    }

    /**
     * Registers set of custom copiers.
     *
     * @param copiers custom copiers
     * @return same builder instance
     */
    public ReflectionClonerBuilder setCopiers(Map<Class<?>, ObjectCopier<?>> copiers) {
        copiers.forEach(this::setCopier);
        return this;
    }

    /**
     * Registers custom copier for type.
     *
     * @param type object type
     * @param copier custom copier
     * @return same builder instance
     */
    public ReflectionClonerBuilder setCopier(Class<?> type, ObjectCopier<?> copier) {
        Check.argNotNull(type, "Type");
        Check.argNotNull(copier, "Copier");
        Check.illegalArg(copiers.get(type) != DEFAULT_COPIERS.get(type), "Copier for %s already set.", type);
        copiers.put(type, copier);
        return this;
    }

    /**
     * Returns value if it is not null or creates with factory.
     *
     * @param <T> value type
     * @param value value
     * @param factory default value factory
     * @return value if it is not null or factory call result
     */
    private static <T> T createIfNull(T value, Supplier<T> factory) {
        return value != null ? value : factory.get();
    }

    /**
     * Creates an instance of the cloner on the basis of the configuration.
     *
     * @return cloner
     */
    public Cloner build() {
        ObjectPolicy objectPolicy = this.objectPolicy;
        if (!objectActions.isEmpty()) {
            Check.illegalArg(objectPolicy != null, "Cannot set object policy and object actions at te same time.");
            objectPolicy = new CustomObjectPolicy(objectActions);
        }

        List<CopyPolicy> policies = new ArrayList<>();
        if (this.policy != null) {
            policies.add(this.policy);
        }
        policies.add(new CustomCopyPolicy(typeActions, fieldActions));
        policies.add(new AnnotatedCopyPolicy());
        CopyPolicy policy = new CompoundCopyPolicy(policies);

        ObjectAllocator allocator = createIfNull(this.allocator, ObjectAllocator::defaultAllocator);
        FieldCopierFactory fieldCopierFactory = createIfNull(this.fieldCopierFactory, ReflectionFieldCopierFactory::new);
        ReflectionCopierProvider provider =
            new ReflectionCopierProvider(objectPolicy, policy, allocator, copiers, fieldCopierFactory);

        Supplier<? extends AbstractCopyContext> contextSupplier;
        CloningMode mode = this.mode != null ? this.mode : CloningMode.SEQUENTIAL;
        switch (mode) {
            case RECURSIVE:
                Check.isNull(this.traversalAlgorithm, "Traversal algorithm must be null for recursive mode.");
                Check.isNull(this.executor, "Executor must be null for recursive mode.");
                contextSupplier = () -> new RecursiveCopyContext(provider);
                break;
            case SEQUENTIAL:
                Check.isNull(this.executor, "Executor must be null for sequential mode.");
                TraversalAlgorithm traversalAlgorithm =
                    this.traversalAlgorithm != null ? this.traversalAlgorithm : TraversalAlgorithm.DEPTH_FIRST;
                contextSupplier = () -> new SequentialCopyContext(provider, traversalAlgorithm);
                break;
            case PARALLEL:
                Check.isNull(this.traversalAlgorithm, "Traversal algorithm must be null for parallel mode.");
                ExecutorService executor = createIfNull(this.executor, ForkJoinPool::commonPool);
                contextSupplier = () -> new ParallelCopyContext(provider, executor);
                break;
            default:
                throw new IllegalStateException();
        }
        return new ClonerImpl(contextSupplier);
    }

}
