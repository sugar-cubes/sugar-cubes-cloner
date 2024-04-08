/*
 * Copyright 2017-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Builder for reflection cloners.
 *
 * @author Maxim Butov
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public final class ReflectionClonerBuilder {

    /**
     * JDK configuration.
     */
    private static final JdkConfiguration JDK_CONFIGURATION = JdkConfigurationHolder.CONFIGURATION;

    /**
     * Default copiers for well-known JDK types.
     */
    private static final Map<Class<?>, ObjectCopier<?>> DEFAULT_COPIERS;

    static {
        Map<Class<?>, ObjectCopier<?>> defaultCopiers = new LinkedHashMap<>();

        JDK_CONFIGURATION.getImmutableTypes().forEach(type -> defaultCopiers.put(type, ObjectCopier.NOOP));
        JDK_CONFIGURATION.getCloneableTypes().forEach(type -> defaultCopiers.put(type, ObjectCopier.CLONEABLE));

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
     * Object factory provider.
     */
    private ObjectFactoryProvider objectFactoryProvider;

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
     * Object copy policy.
     */
    private CopyPolicy<Object> objectPolicy;

    /**
     * Custom actions for objects.
     */
    private final Map<Object, CopyAction> objectActions = new IdentityHashMap<>();

    /**
     * Custom type copy policy.
     */
    private CopyPolicy<Class<?>> typePolicy;

    /**
     * Custom actions for types.
     */
    private final Map<Class<?>, CopyAction> typeActions = new LinkedHashMap<>();

    /**
     * Predicate actions for types.
     */
    private final Map<Predicate<Class<?>>, CopyAction> typePredicateActions = new LinkedHashMap<>();

    /**
     * Custom field copy policy.
     */
    private CopyPolicy<Field> fieldPolicy;

    /**
     * Custom actions for fields.
     */
    private final Map<Field, CopyAction> fieldActions = new LinkedHashMap<>();

    /**
     * Predicate actions for fields.
     */
    private final Map<Predicate<Field>, CopyAction> fieldPredicateActions = new LinkedHashMap<>();

    /**
     * Custom copiers for types.
     */
    private final Map<Class<?>, ObjectCopier<?>> copiers = new LinkedHashMap<>(DEFAULT_COPIERS);

    /**
     * Predefined clones.
     */
    private final Map<Object, Object> clones = new IdentityHashMap<>();

    /**
     * Shallow-mode types.
     */
    private final Set<Class<?>> shallows = new HashSet<>();

    /**
     * Creates a builder.
     */
    public ReflectionClonerBuilder() {
    }

    /**
     * Sets object allocator.
     *
     * @param objectFactoryProvider object factory provider
     * @return same builder instance
     */
    public ReflectionClonerBuilder objectFactoryProvider(ObjectFactoryProvider objectFactoryProvider) {
        this.objectFactoryProvider = check(objectFactoryProvider, this.objectFactoryProvider, "Object factory provider");
        return this;
    }

    /**
     * Sets field copier factory.
     *
     * @param fieldCopierFactory field copier factory
     * @return same builder instance
     */
    public ReflectionClonerBuilder fieldCopierFactory(FieldCopierFactory fieldCopierFactory) {
        this.fieldCopierFactory = check(fieldCopierFactory, this.fieldCopierFactory, "Field copier factory");
        return this;
    }

    /**
     * Sets allocator and field copier factory which use {@link sun.misc.Unsafe}.
     *
     * @return same builder instance
     */
    public ReflectionClonerBuilder unsafe() {
        return objectFactoryProvider(new UnsafeObjectFactoryProvider()).fieldCopierFactory(new UnsafeFieldCopierFactory());
    }

    /**
     * Sets cloning mode.
     *
     * @param mode cloning mode
     * @return same builder instance
     */
    public ReflectionClonerBuilder mode(CloningMode mode) {
        this.mode = check(mode, this.mode, "Mode");
        return this;
    }

    /**
     * Sets traversal algorithm for objects graph.
     *
     * @param traversalAlgorithm traversal algorithm
     * @return same builder instance
     */
    public ReflectionClonerBuilder traversalAlgorithm(TraversalAlgorithm traversalAlgorithm) {
        this.traversalAlgorithm = check(traversalAlgorithm, this.traversalAlgorithm, "Traversal algorithm");
        return this;
    }

    /**
     * Enables parallel mode with given executor service.
     *
     * @param executor executor service
     * @return same builder instance
     */
    public ReflectionClonerBuilder executor(ExecutorService executor) {
        this.executor = check(executor, this.executor, "Executor");
        return this;
    }

    /**
     * Sets object policy. Using object policy significantly slows down cloning process, thus,
     * type and field policies should be used if possible.
     *
     * @param objectPolicy object policy
     * @return same builder instance
     */
    public ReflectionClonerBuilder objectPolicy(CopyPolicy<Object> objectPolicy) {
        this.objectPolicy = check(objectPolicy, this.objectPolicy, "Object policy");
        return this;
    }

    /**
     * Registers action for object. Usage of object actions significantly slows down cloning process, thus,
     * type and field policies should be used if possible.
     *
     * @param original original object
     * @param action action
     * @return same builder instance
     */
    public ReflectionClonerBuilder objectAction(Object original, CopyAction action) {
        Checks.argNotNull(original, "Original");
        Checks.argNotNull(action, "Action");
        Checks.illegalArg(action == CopyAction.SKIP, "SKIP action is not applicable for objects.");
        Checks.illegalArg(objectActions.containsKey(original), "Action for %s already set.", original);
        if (action != CopyAction.DEFAULT) {
            objectActions.put(original, action);
        }
        return this;
    }

    /**
     * Sets type copy policy.
     *
     * @param typePolicy type copy policy
     * @return same builder instance
     */
    public ReflectionClonerBuilder typePolicy(CopyPolicy<Class<?>> typePolicy) {
        this.typePolicy = check(typePolicy, this.typePolicy, "Type policy");
        return this;
    }

    /**
     * Registers action for type.
     *
     * @param type object type
     * @param action action
     * @return same builder instance
     */
    public ReflectionClonerBuilder typeAction(Class<?> type, CopyAction action) {
        Checks.argNotNull(type, "Type");
        Checks.argNotNull(action, "Action");
        Checks.illegalArg(action == CopyAction.SKIP, "SKIP action is not applicable for objects.");
        Checks.illegalArg(typeActions.containsKey(type), "Action for %s already set.", type);
        if (action != CopyAction.DEFAULT) {
            typeActions.put(type, action);
            copiers.remove(type);
        }
        return this;
    }

    /**
     * Registers action for type.
     *
     * @param type object type name
     * @param action action
     * @return same builder instance
     */
    public ReflectionClonerBuilder typeAction(String type, CopyAction action) {
        Checks.argNotNull(type, "Type");
        Checks.argNotNull(action, "Action");
        return typeAction(ClassUtils.classForName(type), action);
    }

    /**
     * Registers action for type predicate.
     *
     * @param typePredicate type predicate
     * @param action action
     * @return same builder instance
     */
    public ReflectionClonerBuilder typeAction(Predicate<Class<?>> typePredicate, CopyAction action) {
        Checks.argNotNull(typePredicate, "Type predicate");
        Checks.argNotNull(action, "Action");
        Checks.illegalArg(action == CopyAction.SKIP, "SKIP action is not applicable for objects.");
        if (action != CopyAction.DEFAULT) {
            typePredicateActions.put(typePredicate, action);
        }
        return this;
    }

    /**
     * Sets field copy policy.
     *
     * @param fieldPolicy type copy policy
     * @return same builder instance
     */
    public ReflectionClonerBuilder fieldPolicy(CopyPolicy<Field> fieldPolicy) {
        Checks.argNotNull(fieldPolicy, "Field policy");
        Checks.isNull(this.fieldPolicy, "Field policy already set.");
        this.fieldPolicy = fieldPolicy;
        return this;
    }

    /**
     * Registers action for field.
     *
     * @param field field
     * @param action action
     * @return same builder instance
     */
    public ReflectionClonerBuilder fieldAction(Field field, CopyAction action) {
        Checks.argNotNull(field, "Field");
        Checks.argNotNull(action, "Action");
        Checks.illegalArg(action == CopyAction.NULL && field.getType().isPrimitive(),
            "Cannot apply action NULL for primitive field %s.", field);
        Checks.illegalArg(fieldActions.containsKey(field), "Action for %s already set.", field);
        if (action != CopyAction.DEFAULT) {
            fieldActions.put(field, action);
        }
        return this;
    }

    /**
     * Registers action for field.
     *
     * @param type object type
     * @param field declared field name
     * @param action action
     * @return same builder instance
     */
    public ReflectionClonerBuilder fieldAction(Class<?> type, String field, CopyAction action) {
        Checks.argNotNull(type, "Type");
        Checks.argNotNull(field, "Field");
        Checks.argNotNull(action, "Action");
        return fieldAction(ReflectionUtils.getField(type, field), action);
    }

    /**
     * Registers action for field predicate.
     *
     * @param fieldPredicate field predicate
     * @param action action
     * @return same builder instance
     */
    public ReflectionClonerBuilder fieldAction(Predicate<Field> fieldPredicate, CopyAction action) {
        Checks.argNotNull(fieldPredicate, "Field predicate");
        Checks.argNotNull(action, "Action");
        if (action != CopyAction.DEFAULT) {
            fieldPredicateActions.put(fieldPredicate, action);
        }
        return this;
    }

    /**
     * Registers set of copiers.
     *
     * @param copiers copiers
     * @return same builder instance
     */
    public ReflectionClonerBuilder copiers(Map<Class<?>, ObjectCopier<?>> copiers) {
        copiers.forEach(this::copier);
        return this;
    }

    /**
     * Registers copier for type.
     *
     * @param type object type
     * @param copier copier
     * @return same builder instance
     */
    public ReflectionClonerBuilder copier(Class<?> type, ObjectCopier<?> copier) {
        Checks.argNotNull(type, "Type");
        Checks.argNotNull(copier, "Copier");
        Checks.illegalArg(copiers.get(type) != DEFAULT_COPIERS.get(type), "Copier for %s already set.", type);
        copiers.put(type, copier);
        return this;
    }

    /**
     * Registers clone for the object.
     *
     * @param original original object
     * @param clone clone
     * @return same builder instance
     */
    public ReflectionClonerBuilder clone(Object original, Object clone) {
        Checks.argNotNull(original, "Original");
        Checks.argNotNull(clone, "Clone");
        Checks.illegalArg(clones.get(original) != null, "Clone for %s already set.", original);
        clones.put(original, clone);
        return this;
    }

    /**
     * Registers singleton, i.e. the object which must be only copied by reference.
     *
     * @param singleton singleton
     * @return same builder instance
     */
    public ReflectionClonerBuilder singleton(Object singleton) {
        Checks.argNotNull(singleton, "Singleton");
        Checks.illegalArg(clones.get(singleton) != null, "Singleton %s already registered.", singleton);
        clones.put(singleton, singleton);
        return this;
    }

    /**
     * Registers type to be copied in shallow mode, i.e. field values won't be cloned.
     *
     * @param type type
     * @return same builder instance
     */
    public ReflectionClonerBuilder shallow(Class<?> type) {
        Checks.argNotNull(type, "Type");
        Checks.illegalArg(typeActions.containsKey(type), "Action for %s already set.", type);
        Checks.illegalArg(copiers.get(type) != DEFAULT_COPIERS.get(type), "Copier for %s already set.", type);
        Checks.illegalArg(shallows.contains(type), "Shallow mode already enabled for %s.", type);
        copiers.remove(type);
        shallows.add(type);
        return this;
    }

    /**
     * Checks new value to be not null and old value to be null.
     *
     * @param <T> argument type
     * @param value new value
     * @param oldValue old value
     * @param arg argument name
     * @return new value
     */
    private static <T> T check(T value, T oldValue, String arg) {
        Checks.argNotNull(value, arg);
        Checks.isNull(oldValue, "%s already set.", arg);
        return value;
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
     * Creates compound policy.
     *
     * @param <I> input object type
     * @param policy custom policy
     * @param exactMap map (object, copy action)
     * @param predicateMap map (predicate, copy action)
     * @param fallbackPolicy fallback policy
     * @return compound policy
     */
    private static <I> CopyPolicy<I> compound(CopyPolicy<I> policy, Map<I, CopyAction> exactMap,
        Map<Predicate<I>, CopyAction> predicateMap, CopyPolicy<I> fallbackPolicy) {
        List<CopyPolicy<I>> policies = new ArrayList<>();
        if (policy != null) {
            policies.add(policy);
        }
        if (!exactMap.isEmpty()) {
            policies.add(new ExactPolicy<>(exactMap));
        }
        if (!predicateMap.isEmpty()) {
            policies.add(new PredicatePolicy<>(predicateMap));
        }
        if (fallbackPolicy != null) {
            policies.add(fallbackPolicy);
        }
        return CopyPolicy.compound(policies);
    }

    /**
     * Creates an instance of the cloner on the basis of the configuration.
     *
     * @return cloner
     */
    public Cloner build() {
        CopyPolicy<Object> objectPolicy;
        if (this.objectPolicy != null || !objectActions.isEmpty()) {
            objectPolicy = compound(this.objectPolicy, objectActions, Collections.emptyMap(), null);
        }
        else {
            objectPolicy = null;
        }

        CopyPolicy<Class<?>> typePolicy = compound(this.typePolicy, typeActions, typePredicateActions, new AnnotatedTypeCopyPolicy());

        CopyPolicy<Field> fieldPolicy = compound(this.fieldPolicy, fieldActions, fieldPredicateActions, new AnnotatedFieldCopyPolicy());

        ObjectFactoryProvider objectFactoryProvider = createIfNull(this.objectFactoryProvider, ObjectFactoryProvider::defaultInstance);
        FieldCopierFactory fieldCopierFactory = createIfNull(this.fieldCopierFactory, ReflectionFieldCopierFactory::new);
        ReflectionCopierProvider provider =
            new ReflectionCopierProvider(objectPolicy, typePolicy, fieldPolicy, objectFactoryProvider, copiers, shallows, fieldCopierFactory);

        Supplier<? extends AbstractCopyContext> contextSupplier;
        CloningMode mode = this.mode != null ? this.mode : CloningMode.SEQUENTIAL;
        switch (mode) {
            case RECURSIVE:
                Checks.isNull(this.traversalAlgorithm, "Traversal algorithm must be null for recursive mode.");
                Checks.isNull(this.executor, "Executor must be null for recursive mode.");
                contextSupplier = () -> new RecursiveCopyContext(provider, clones);
                break;
            case SEQUENTIAL:
                Checks.isNull(this.executor, "Executor must be null for sequential mode.");
                TraversalAlgorithm traversalAlgorithm = this.traversalAlgorithm != null ? this.traversalAlgorithm : TraversalAlgorithm.DEPTH_FIRST;
                contextSupplier = () -> new SequentialCopyContext(provider, clones, traversalAlgorithm);
                break;
            case PARALLEL:
                Checks.isNull(this.traversalAlgorithm, "Traversal algorithm must be null for parallel mode.");
                ExecutorService executor = createIfNull(this.executor, ForkJoinPool::commonPool);
                contextSupplier = () -> new ParallelCopyContext(provider, clones, executor);
                break;
            default:
                throw Checks.mustNotHappen();
        }
        return new ClonerImpl(contextSupplier);
    }

}
