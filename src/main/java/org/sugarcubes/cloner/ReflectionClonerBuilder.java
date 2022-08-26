package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
     * Traversal algorithm for sequential mode.
     */
    private TraversalAlgorithm traversalAlgorithm;

    /**
     * Executor service for parallel mode.
     */
    private ExecutorService executor;

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
     * Enable annotations processing.
     */
    private boolean annotated;

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
        Check.isNull(this.allocator, "Allocator already set");
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
        Check.isNull(this.fieldCopierFactory, "Field copier factory already set");
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
     * Sets traversal algorithm for objects graph.
     *
     * @param traversalAlgorithm traversal algorithm
     * @return same builder instance
     */
    public ReflectionClonerBuilder setTraversalAlgorithm(TraversalAlgorithm traversalAlgorithm) {
        Check.argNotNull(traversalAlgorithm, "Traversal algorithm");
        Check.isNull(this.traversalAlgorithm, "Traversal algorithm already set");
        Check.isNull(this.executor, "Cannot set traversal algorithm for parallel mode");
        this.traversalAlgorithm = traversalAlgorithm;
        return this;
    }

    /**
     * Registers set of custom copiers.
     *
     * @param copiers custom copiers
     * @return same builder instance
     */
    public ReflectionClonerBuilder setCopiers(Map<Class<?>, ObjectCopier<?>> copiers) {
        copiers.forEach(this::setObjectCopier);
        return this;
    }

    /**
     * Registers custom copier for type.
     *
     * @param type object type
     * @param copier custom copier
     * @return same builder instance
     */
    public ReflectionClonerBuilder setObjectCopier(Class<?> type, ObjectCopier<?> copier) {
        Check.argNotNull(type, "Type");
        Check.argNotNull(copier, "Copier");
        Check.illegalArg(copiers.put(type, copier) != DEFAULT_COPIERS.get(type), "Copier for %s already set", type);
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
        Check.isNull(this.executor, "Executor already set");
        Check.isNull(this.traversalAlgorithm, "");
        this.executor = executor;
        return this;
    }

    /**
     * Enables parallel mode with default executor.
     *
     * @return same builder instance
     */
    public ReflectionClonerBuilder setDefaultExecutor() {
        return setExecutor(ForkJoinPool.commonPool());
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
        Check.isNull(typeActions.put(type, action), "Action for %s already set.", type);
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
            "Cannot set action NULL for primitive field");
        Check.isNull(fieldActions.put(field, action), "Action for %s already set.", field);
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
     * Enables annotations processing.
     *
     * @return same builder instance
     */
    public ReflectionClonerBuilder setAnnotated() {
        Check.illegalArg(annotated, "Already annotated");
        annotated = true;
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
        ObjectAllocator allocator = createIfNull(this.allocator, ObjectAllocator::defaultAllocator);
        CopyPolicy policy = annotated ?
            new AnnotatedCopyPolicy(typeActions, fieldActions) :
            new DefaultCopyPolicy(typeActions, fieldActions);
        FieldCopierFactory fieldCopierFactory = createIfNull(this.fieldCopierFactory, ReflectionFieldCopierFactory::new);
        ReflectionCopierRegistry registry = annotated ?
            new AnnotatedCopierRegistry(policy, allocator, copiers, fieldCopierFactory) :
            new ReflectionCopierRegistry(policy, allocator, copiers, fieldCopierFactory);
        if (executor == null) {
            return new SequentialReflectionCloner(registry,
                traversalAlgorithm != null ? traversalAlgorithm : TraversalAlgorithm.DEPTH_FIRST);
        }
        else {
            return new ParallelReflectionCloner(registry, executor);
        }

    }

}
