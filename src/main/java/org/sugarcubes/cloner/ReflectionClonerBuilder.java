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

import org.sugarcubes.cloner.annotation.AnnotatedCopierRegistry;
import org.sugarcubes.cloner.annotation.AnnotatedCopyPolicy;
import static org.sugarcubes.cloner.Check.argNotNull;
import static org.sugarcubes.cloner.Check.illegalArg;
import static org.sugarcubes.cloner.Check.isNull;

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

    private static final Map<Class<?>, ObjectCopier<?>> DEFAULT_COPIERS;

    static {
        Map<Class<?>, ObjectCopier<?>> defaultCopiers = new HashMap<>();

        IMMUTABLE_TYPES.forEach(type -> defaultCopiers.put(type, ObjectCopier.NOOP));

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

    private ObjectAllocator allocator;
    private FieldCopierFactory fieldCopierFactory;
    private TraversalAlgorithm traversalAlgorithm;
    private ExecutorService executor;

    private final Map<Field, FieldCopyAction> fieldActions = new HashMap<>();
    private final Map<Class<?>, ObjectCopier<?>> copiers = new HashMap<>(DEFAULT_COPIERS);

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
        Check.isNull(this.traversalAlgorithm, "Traversal algorithm already set");
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
        illegalArg(copiers.put(type, copier) != DEFAULT_COPIERS.get(type), "Copier for %s already set", type);
        return this;
    }

    /**
     * Enables parallel mode with given executor service.
     *
     * @param executor executor service
     * @return same builder instance
     */
    public ReflectionClonerBuilder setExecutor(ExecutorService executor) {
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
        argNotNull(type, "Type");
        argNotNull(action, "Action");
        switch (action) {
            case NULL:
                return setObjectCopier(type, ObjectCopier.NULL);
            case ORIGINAL:
                return setObjectCopier(type, ObjectCopier.NOOP);
            case DEFAULT:
                return this;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Registers custom action for type.
     *
     * @param type object type
     * @param action custom action
     * @return same builder instance
     */
    public ReflectionClonerBuilder setTypeAction(String type, CopyAction action) {
        argNotNull(type, "Type");
        argNotNull(action, "Action");
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
        argNotNull(field, "Field");
        argNotNull(action, "Action");
        illegalArg(field.getType().isPrimitive() && action == FieldCopyAction.NULL,
            "Cannot set action NULL for primitive field");
        isNull(fieldActions.put(field, action), "Action for %s already set.", field);
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
        argNotNull(type, "Type");
        argNotNull(field, "Field");
        return setFieldAction(ReflectionUtils.getField(type, field), action);
    }

    /**
     * Enables annotations processing.
     *
     * @return same builder instance
     */
    public ReflectionClonerBuilder setAnnotated() {
        illegalArg(annotated, "Already annotated");
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
            new AnnotatedCopyPolicy(fieldActions) :
            new DefaultCopyPolicy(fieldActions);
        FieldCopierFactory fieldCopierFactory = createIfNull(this.fieldCopierFactory, ReflectionFieldCopierFactory::new);
        ReflectionCopierRegistry registry = annotated ?
            new AnnotatedCopierRegistry(policy, allocator, copiers, fieldCopierFactory) :
            new ReflectionCopierRegistry(policy, allocator, copiers, fieldCopierFactory);
        Supplier<CompletableCopyContext> contextFactory;
        if (executor != null) {
            contextFactory = () -> new ParallelCopyContext(registry, executor);
        }
        else {
            contextFactory = () -> new SequentialCopyContext(registry,
                traversalAlgorithm != null ? traversalAlgorithm : TraversalAlgorithm.DEPTH_FIRST);
        }

        return new ReflectionCloner(contextFactory);
    }

}
