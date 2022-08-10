package org.sugarcubes.cloner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import static org.sugarcubes.cloner.Check.argNotNull;
import static org.sugarcubes.cloner.CloningPolicyHelper.isComponentTypeImmutable;

/**
 * The implementation of {@link Cloner} which uses Java reflection API for cloning.
 *
 * @author Maxim Butov
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class ReflectionCloner implements Cloner {

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
     * Object graph traversal algorithm.
     */
    private TraversalAlgorithm traversalAlgorithm = TraversalAlgorithm.DEPTH_FIRST;

    /**
     * Executor service.
     */
    private ExecutorService executor;

    /**
     * Cache of copiers.
     */
    private final LazyCache<Class<?>, ObjectCopier<?>> copiers = new LazyCache<>(DEFAULT_COPIERS, this::getCopier);

    /**
     * Cache of reflection copiers.
     */
    private final LazyCache<Class<?>, ReflectionCopier<?>> reflectionCopiers = new LazyCache<>(this::newReflectionCopier);

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
        // one can replace default copiers only
        if (copiers.put(type, copier) != DEFAULT_COPIERS.get(type)) {
            throw new IllegalArgumentException(String.format("Copier for %s already set.", type.getName()));
        }
        return this;
    }

    /**
     * Sets traversal algorithm for objects graph.
     *
     * @param traversalAlgorithm traversal algorithm
     * @return same cloner instance
     */
    public ReflectionCloner traversal(TraversalAlgorithm traversalAlgorithm) {
        this.traversalAlgorithm = argNotNull(traversalAlgorithm, "Traversal algorithm");
        return this;
    }

    /**
     * Enables parallel mode with given executor service.
     *
     * @param executor executor service
     * @return same cloner instance
     */
    public ReflectionCloner parallel(ExecutorService executor) {
        if (this.executor != null) {
            throw new IllegalStateException("Already parallel.");
        }
        this.executor = argNotNull(executor, "Executor");
        return this;
    }

    /**
     * Enables parallel mode.
     *
     * @return same cloner instance
     */
    public ReflectionCloner parallel() {
        return parallel(ForkJoinPool.commonPool());
    }

    @Override
    public <T> T clone(T object) {
        try {
            CompletableCopyContext context;
            CopierRegistry registry = copiers::get;
            if (executor != null) {
                context = new ParallelCopyContext(registry, executor);
            }
            else {
                context = new SequentialCopyContext(registry, traversalAlgorithm);
            }
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
                    return isComponentTypeImmutable(policy, type.getComponentType()) ?
                        ObjectCopier.SHALLOW : ObjectCopier.OBJECT_ARRAY;
                }
                else {
                    return reflectionCopiers.get(type);
                }
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Creates {@link ReflectionCopier} instance for the type.
     *
     * @param <T> object type
     * @param type object type
     * @return copier instance
     */
    protected <T> ReflectionCopier<T> newReflectionCopier(Class<T> type) {
        if (type == null) {
            return null;
        }
        ReflectionCopier<? super T> superCopier = newReflectionCopier(type.getSuperclass());
        return newReflectionCopier(type, superCopier);
    }

    /**
     * Creates {@link ReflectionCopier} instance for the type.
     *
     * @param <T> object type
     * @param type object type
     * @param superCopier copier for super type
     * @return copier instance
     */
    protected <T> ReflectionCopier<T> newReflectionCopier(Class<T> type, ReflectionCopier<? super T> superCopier) {
        return new ReflectionCopier<>(allocator, policy, type, superCopier);
    }

}
