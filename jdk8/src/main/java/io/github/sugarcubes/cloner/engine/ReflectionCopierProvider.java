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
package io.github.sugarcubes.cloner.engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import io.github.sugarcubes.cloner.CopyAction;
import io.github.sugarcubes.cloner.CopyPolicy;
import io.github.sugarcubes.cloner.Copyable;
import io.github.sugarcubes.cloner.NoopCopier;
import io.github.sugarcubes.cloner.NullCopier;
import io.github.sugarcubes.cloner.ObjectCopier;
import io.github.sugarcubes.cloner.Toolset;
import io.github.sugarcubes.cloner.TypeCopier;
import io.github.sugarcubes.cloner.spi.ContainerModel;
import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.spi.ValuesModel;
import io.github.sugarcubes.cloner.util.Checks;
import io.github.sugarcubes.cloner.util.ClonerExceptionUtils;
import io.github.sugarcubes.cloner.util.ConcurrentLazyCache;
import io.github.sugarcubes.cloner.util.Lazy;

/**
 * Copier provider implementation.
 *
 * @author Maxim Butov
 */
public class ReflectionCopierProvider implements CopierProvider {

    /**
     * Toolset.
     */
    private final Toolset toolset;

    /**
     * Object policy.
     */
    private final CopyPolicy<Object> objectPolicy;

    /**
     * Type policy.
     */
    private final CopyPolicy<Class<?>> typePolicy;

    /**
     * Field policy.
     */
    private final CopyPolicy<Field> fieldPolicy;

    /**
     * Cache of copiers.
     */
    private final ConcurrentLazyCache<Class<?>, ObjectCopier<?>> copiers = new ConcurrentLazyCache<>(this::findCopier);

    /**
     * Shallow mode types.
     */
    private final Set<Class<?>> shallows;

    /**
     * Cache of reflection copiers.
     */
    private final Map<Map.Entry<Class<?>, Boolean>, FieldsCopier<?>> fieldsCopiers = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param toolset reflection tool set
     * @param objectPolicy object policy
     * @param typePolicy type policy
     * @param fieldPolicy field policy
     * @param copiers predefined copiers
     * @param shallows shallow-mode types
     */
    public ReflectionCopierProvider(Toolset toolset, CopyPolicy<Object> objectPolicy, CopyPolicy<Class<?>> typePolicy,
        CopyPolicy<Field> fieldPolicy, Map<Class<?>, ObjectCopier<?>> copiers, Set<Class<?>> shallows) {
        this.toolset = toolset;
        this.objectPolicy = objectPolicy;
        this.typePolicy = typePolicy;
        this.fieldPolicy = fieldPolicy;
        this.copiers.putAll(copiers);
        this.shallows = shallows;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ObjectCopier<T> getCopier(T original) {
        if (objectPolicy != null) {
            return choose(original, processAction(objectPolicy.getAction(original), () -> copiers.get(original.getClass())));
        }
        return choose(original, copiers.get(original.getClass()));
    }

    /**
     * Returns the copier chosen for the object, if the copier of the type chooses.
     */
    @SuppressWarnings("unchecked")
    private static <T> ObjectCopier<T> choose(T original, ObjectCopier<?> copier) {
        return copier instanceof ChoosingCopier
            ? ((ChoosingCopier<T>) copier).choose(original)
            : (ObjectCopier<T>) copier;
    }

    /**
     * Finds or creates copier if it was not created yet.
     *
     * @param type object type
     * @return copier
     */
    private ObjectCopier<?> findCopier(Class<?> type) {
        return processAction(typePolicy.getAction(type), () -> findCopierForType(type));
    }

    /**
     * Object copier for {@link CopyAction#NULL} or {@link CopyAction#ORIGINAL} can be returned instantly. For the
     * {@link CopyAction#DEFAULT} action #defaultCopierSupplier will be called.
     *
     * @param action copy action
     * @param defaultCopierSupplier supplier of default copier
     * @return object copier
     */
    private ObjectCopier<?> processAction(CopyAction action, Supplier<ObjectCopier<?>> defaultCopierSupplier) {
        switch (action) {
            case SKIP:
                throw new IllegalStateException("SKIP action is not applicable for objects.");
            case NULL:
                return ObjectCopier.NULL;
            case ORIGINAL:
                return ObjectCopier.NOOP;
            case DEFAULT:
                return defaultCopierSupplier.get();
            default:
                throw Checks.mustNotHappen();
        }
    }

    /**
     * Finds copier for type.
     *
     * @param type type
     * @return object copier
     */
    private ObjectCopier<?> findCopierForType(Class<?> type) {
        if (Enum.class.isAssignableFrom(type)) {
            return ObjectCopier.NOOP;
        }
        if (type.isArray()) {
            return findArrayCopier(type);
        }
        TypeCopier annotation = type.getDeclaredAnnotation(TypeCopier.class);
        if (annotation != null) {
            return createCopierFromAnnotation(annotation);
        }
        if (Copyable.class.isAssignableFrom(type)) {
            return ObjectCopier.COPYABLE;
        }
        if (toolset.isHidden(type)) {
            // instances of hidden classes (e.g. lambdas) can be neither created nor written, they are shared
            return ObjectCopier.NOOP;
        }
        return findModelCopier(type);
    }

    /**
     * Finds copier for an array type.
     */
    private ObjectCopier<?> findArrayCopier(Class<?> type) {
        Class<?> componentType = type.getComponentType();
        if (componentType.isPrimitive() || componentType.isEnum()) {
            return ObjectCopier.CLONEABLE;
        }
        if (Modifier.isFinal(componentType.getModifiers()) &&
            typePolicy.getAction(componentType) == CopyAction.ORIGINAL) {
            // there is no mutable subtype of componentType, so, we can shallow clone array
            return ObjectCopier.CLONEABLE;
        }
        return ObjectCopier.OBJECT_ARRAY;
    }

    /**
     * Finds copier by the low-level model of the type: values, container or fields.
     */
    private ObjectCopier<?> findModelCopier(Class<?> type) {
        boolean shallow = shallows.contains(type);
        Optional<? extends ValuesModel<?>> values = toolset.valuesModel(type);
        if (values.isPresent()) {
            return valuesCopier(values.get(), type, shallow);
        }
        Optional<? extends ContainerModel<?>> container = toolset.containerModel(type);
        if (container.isPresent()) {
            return containerCopier(container.get(), type, shallow);
        }
        return findFieldsCopier(type, shallow);
    }

    /**
     * Creates objects from values; collections which hash their keys are copied by fields if hash codes of the copied
     * keys are not known at once.
     */
    private <T> ObjectCopier<T> valuesCopier(ValuesModel<T> model, Class<?> type, boolean shallow) {
        if (!model.hashesKeys()) {
            return new ValuesCopier<>(model, shallow);
        }
        return new HashValuesCopier<>(model, lazyFieldsCopier(type, shallow), shallow);
    }

    /**
     * Copier by fields created when first needed: making fields of JDK classes accessible needs opened packages.
     */
    @SuppressWarnings("unchecked")
    private <T> Supplier<FieldsCopier<T>> lazyFieldsCopier(Class<?> type, boolean shallow) {
        return new Lazy<>(() -> (FieldsCopier<T>) findFieldsCopier(type, shallow));
    }

    /**
     * Rebuilds containers which do not hash their keys, copies by fields and checks containers which hash them.
     */
    @SuppressWarnings("unchecked")
    private <T> ObjectCopier<T> containerCopier(ContainerModel<T> model, Class<?> type, boolean shallow) {
        if (!model.hashesKeys()) {
            return new ContainerCopier<>(model, shallow);
        }
        return new HashContainerCopier<>(lazyFieldsCopier(type, shallow), model, shallow);
    }

    /**
     * Creates an instance of object copier on the basis of annotation properties.
     *
     * @param annotation annotation
     * @return object copier
     */
    private ObjectCopier<?> createCopierFromAnnotation(TypeCopier annotation) {
        Class<? extends ObjectCopier<?>> copierClass = (Class) annotation.value();
        if (NullCopier.class.equals(copierClass)) {
            return ObjectCopier.NULL;
        }
        if (NoopCopier.class.equals(copierClass)) {
            return ObjectCopier.NOOP;
        }
        // the copier class belongs to the user and may be not public
        Reflection reflection = toolset.getReflection();
        Constructor<? extends ObjectCopier<?>> constructor =
            reflection.makeAccessible(reflection.getDeclaredConstructor(copierClass));
        return ClonerExceptionUtils.replaceException(() -> constructor.newInstance());
    }

    /**
     * Returns {@link FieldsCopier} instance for the type.
     *
     * @param type object type
     * @return copier instance
     */
    private FieldsCopier<?> findFieldsCopier(Class<?> type, boolean shallow) {
        Map.Entry<Class<?>, Boolean> key = (Map.Entry) Collections.singletonMap(type, shallow).entrySet().iterator().next();
        FieldsCopier<?> copier = fieldsCopiers.get(key);
        if (copier == null) {
            Class<?> superType = type.getSuperclass();
            FieldsCopier<?> parent = superType != null ? findFieldsCopier(superType, shallow) : null;
            copier = new FieldsCopier<>(parent, toolset.fieldsModel(type), fieldPolicy, shallow);
            fieldsCopiers.put(key, copier);
        }
        return copier;
    }

}
