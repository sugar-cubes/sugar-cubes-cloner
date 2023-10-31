/*
 * Copyright 2017-2023 the original author or authors.
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
import java.lang.reflect.Modifier;
import java.util.Arrays;

import io.github.sugarcubes.cloner.internal.InstanceAllocator;

/**
 * Copier which creates object with {@link #factory}, when copying, enumerates fields,
 * filters by policy and copies via reflection.
 *
 * @author Maxim Butov
 */
public class ReflectionCopier<T> extends TwoPhaseObjectCopier<T> {

    /**
     * Object factory of the type.
     */
    private final InstanceAllocator<T> factory;

    /**
     * Super type cloner, nullable.
     */
    private final ReflectionCopier<? super T> parent;

    /**
     * List of field copiers.
     */
    private final FieldCopier[] fieldCopiers;

    /**
     * Creates reflection copier.
     *
     * @param parent copier for the super type
     * @param toolset reflection tool set
     * @param type object type
     * @param policy copy policy
     * @param shallow shallow mode
     */
    @SuppressWarnings("unchecked")
    public ReflectionCopier(ReflectionCopier<?> parent, Toolset toolset, Class<T> type, CopyPolicy<Field> policy, boolean shallow) {
        this.factory = toolset.getAllocator().newAllocator(type);
        this.parent = (ReflectionCopier<? super T>) (parent != null && parent.fieldCopiers.length == 0 ?
            parent.parent : parent);
        CopyPolicy<Field> fieldPolicy = shallow
            ? CopyPolicy.compound(policy, CopyPolicy.original())
            : policy;
        this.fieldCopiers = Arrays.stream(toolset.getReflection().getDeclaredFields(type))
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .map(field -> toolset.getFieldCopier().getFieldCopier(field, fieldPolicy.getAction(field)))
            .filter(copier -> copier != FieldCopier.NOOP)
            .toArray(FieldCopier[]::new);
    }

    @Override
    public T allocate(T original) throws Exception {
        return factory.newInstance();
    }

    @Override
    public void deepCopy(T original, T clone, CopyContext context) throws Exception {
        if (parent != null) {
            parent.deepCopy(original, clone, context);
        }
        for (FieldCopier field : fieldCopiers) {
            field.copy(original, clone, context);
        }
    }

}
