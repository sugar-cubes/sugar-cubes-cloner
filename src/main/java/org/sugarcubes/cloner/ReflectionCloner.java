package org.sugarcubes.cloner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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

        defaultCopiers.put(java.util.ArrayDeque.class, new SimpleCollectionCopier<>(java.util.ArrayDeque::new));
        defaultCopiers.put(java.util.ArrayList.class, new SimpleCollectionCopier<>(java.util.ArrayList::new));
        defaultCopiers.put(java.util.LinkedList.class, new SimpleCollectionCopier<>(size -> new java.util.LinkedList<>()));
        defaultCopiers.put(java.util.Stack.class, new SimpleCollectionCopier<>(size -> new java.util.Stack<>()));
        defaultCopiers.put(java.util.Vector.class, new SimpleCollectionCopier<>(java.util.Vector::new));

        defaultCopiers.put(java.util.IdentityHashMap.class, new IdentityHashMapCopier());

        defaultCopiers.put(ReflectionUtils.classForName("java.util.RegularEnumSet"), ObjectCopier.SHALLOW);
        defaultCopiers.put(ReflectionUtils.classForName("java.util.JumboEnumSet"), ObjectCopier.SHALLOW);

        DEFAULT_COPIERS = Collections.unmodifiableMap(defaultCopiers);
    }

    protected final ObjectAllocator allocator;
    protected final CloningPolicy policy;

    private TraversalAlgorithm traversalAlgorithm = TraversalAlgorithm.DEPTH_FIRST;
    private ForkJoinPool pool;

    private final LazyCache<Class<?>, ObjectCopier<?>> copiers;

    /**
     * Constructor.
     */
    public ReflectionCloner() {
        this(ObjectAllocator.defaultAllocator(), new CustomCloningPolicy());
    }

    /**
     * Constructor.
     *
     * @param allocator object allocator
     */
    public ReflectionCloner(ObjectAllocator allocator) {
        this(allocator, new CustomCloningPolicy());
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
        this.copiers = new LazyCache<>(this::findCopier);
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
        // one can replace default copiers
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
     * Enable parallel mode with given fork-join pool.
     *
     * @param pool fork-join pool
     * @return same cloner instance
     */
    public ReflectionCloner parallel(ForkJoinPool pool) {
        if (this.pool != null) {
            throw new IllegalStateException("Already parallel.");
        }
        argNotNull(pool, "Pool");
        this.pool = pool;
        return this;
    }

    /**
     * Enable parallel mode with common fork-join pool.
     *
     * @return same cloner instance
     */
    public ReflectionCloner parallel() {
        return parallel(ForkJoinPool.commonPool());
    }

    @Override
    public <T> T clone(T object) {
        try {
            if (pool == null) {
                SequentialCopyContext context = new SequentialCopyContext(this::getCopier, traversalAlgorithm);
                T clone = context.copy(object);
                context.complete();
                return clone;
            }
            else {
                ParallelCopyContext<T> context = new ParallelCopyContext<>(this::getCopier, object);
                T clone = pool.invoke(context);
                context.complete();
                return clone;
            }
        }
        catch (ClonerException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ClonerException(e);
        }
    }

    protected <T> ObjectCopier<T> getCopier(Class<T> type) {
        return (ObjectCopier) copiers.get(type);
    }

    /**
     * Creates copier for the type.
     *
     * @param type type
     * @return copier
     */
    protected <T> ObjectCopier<T> findCopier(Class<T> type) {
        CopyAction action = policy.getTypeAction(type);
        ObjectCopier<?> copier;
        switch (action) {
            case NULL:
                copier = ObjectCopier.NULL;
                break;
            case ORIGINAL:
                copier = ObjectCopier.NOOP;
                break;
            case DEFAULT:
                if (type.isArray()) {
                    copier = isComponentTypeImmutable(policy, type.getComponentType()) ?
                        ObjectCopier.SHALLOW : ObjectCopier.OBJECT_ARRAY;
                }
                else {
                    copier = reflectionCopiers.get(type);
                }
                break;
            default:
                throw new IllegalStateException();
        }
        return (ObjectCopier<T>) copier;
    }

    private final LazyCache<Class<?>, ReflectionCopier<?>> reflectionCopiers = new LazyCache<>(this::findReflectionCopier);

    protected <T> ReflectionCopier<T> findReflectionCopier(Class<T> type) {
        if (type == null) {
            return null;
        }
        ReflectionCopier<? super T> superCopier = findReflectionCopier(type.getSuperclass());
        return newReflectionCopier(type, superCopier);
    }

    protected <T> ReflectionCopier<T> newReflectionCopier(Class<T> type, ReflectionCopier<? super T> superCopier) {
        return new ReflectionCopier<>(allocator, policy, type, superCopier);
    }

}
