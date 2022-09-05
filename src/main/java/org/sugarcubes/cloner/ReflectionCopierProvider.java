/*
 * Copyright 2017-2022 the original author or authors.
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
package org.sugarcubes.cloner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Copier provider implementation.
 *
 * @author Maxim Butov
 */
public class ReflectionCopierProvider implements CopierProvider {

    /**
     * Object policy.
     */
    private final ObjectPolicy objectPolicy;

    /**
     * Copy policy.
     */
    private final CopyPolicy policy;

    /**
     * Object allocator.
     */
    private final ObjectAllocator allocator;

    /**
     * Field copier factory.
     */
    private final FieldCopierFactory fieldCopierFactory;

    /**
     * Cache of copiers.
     */
    private final ConcurrentLazyCache<Class<?>, ObjectCopier<?>> copiers = new ConcurrentLazyCache<>(this::findCopier);

    /**
     * Cache of reflection copiers.
     */
    private final Map<Class<?>, ReflectionCopier<?>> reflectionCopiers = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param objectPolicy object policy
     * @param policy copy policy
     * @param allocator object allocator
     * @param copiers predefined copiers
     * @param fieldCopierFactory field copier factory
     */
    public ReflectionCopierProvider(ObjectPolicy objectPolicy, CopyPolicy policy, ObjectAllocator allocator, Map<Class<?>,
        ObjectCopier<?>> copiers, FieldCopierFactory fieldCopierFactory) {
        this.objectPolicy = objectPolicy;
        this.policy = policy;
        this.allocator = allocator;
        this.fieldCopierFactory = fieldCopierFactory;
        this.copiers.putAll(copiers);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ObjectCopier<T> getCopier(T original) {
        if (objectPolicy != null) {
            return (ObjectCopier<T>) processAction(objectPolicy.getObjectAction(original),
                () -> copiers.get(original.getClass()));
        }
        return (ObjectCopier<T>) copiers.get(original.getClass());
    }

    /**
     * Finds or creates copier if it was not created yet.
     *
     * @param type object type
     * @return copier
     */
    private ObjectCopier<?> findCopier(Class<?> type) {
        return processAction(policy.getTypeAction(type), () -> findCopierForType(type));
    }

    private ObjectCopier<?> processAction(CopyAction action, Supplier<ObjectCopier<?>> defaultCopierSupplier) {
        switch (action) {
            case NULL:
                return ObjectCopier.NULL;
            case ORIGINAL:
                return ObjectCopier.NOOP;
            case DEFAULT:
                return defaultCopierSupplier.get();
            default:
                throw new IllegalStateException();
        }
    }

    private ObjectCopier<?> findCopierForType(Class<?> type) {
        if (type.isEnum()) {
            return ObjectCopier.NOOP;
        }
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
        TypeCopier annotation = type.getDeclaredAnnotation(TypeCopier.class);
        if (annotation != null) {
            return createCopierFromAnnotation(annotation);
        }
        if (Copyable.class.isAssignableFrom(type)) {
            return ObjectCopier.COPYABLE;
        }
        return findReflectionCopier(type);
    }

    private ObjectCopier<?> createCopierFromAnnotation(TypeCopier annotation) {
        Class<? extends ObjectCopier<?>> copierClass = annotation.value();
        Constructor<? extends ObjectCopier<?>> constructor = ReflectionUtils.getConstructor(copierClass);
        return ReflectionUtils.execute(constructor::newInstance);
    }

    /**
     * Returns {@link ReflectionCopier} instance for the type.
     *
     * @param type object type
     * @return copier instance
     */
    private ReflectionCopier<?> findReflectionCopier(Class<?> type) {
        ReflectionCopier<?> copier = reflectionCopiers.get(type);
        if (copier == null) {
            Class<?> superType = type.getSuperclass();
            ReflectionCopier<?> parent = superType != null ? findReflectionCopier(superType) : null;
            copier = new ReflectionCopier<>(policy, allocator, type, fieldCopierFactory, parent);
            reflectionCopiers.put(type, copier);
        }
        return copier;
    }

}
