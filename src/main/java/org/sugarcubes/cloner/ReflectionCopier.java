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

import java.util.Arrays;

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
    private final ObjectFactory<T> factory;

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
     * @param policy copy policy
     * @param allocator object allocator
     * @param type object type
     * @param fieldCopierFactory field copier factory
     * @param parent copier for the super type
     */
    @SuppressWarnings("unchecked")
    public ReflectionCopier(CopyPolicy policy, ObjectAllocator allocator, Class<T> type, FieldCopierFactory fieldCopierFactory,
        ReflectionCopier<?> parent) {
        this.factory = allocator.getFactory(type);
        this.parent = (ReflectionCopier<? super T>) (parent != null && parent.fieldCopiers.length == 0 ?
            parent.parent : parent);
        this.fieldCopiers = Arrays.stream(type.getDeclaredFields())
            .filter(ReflectionUtils::isNonStatic)
            .map(field -> fieldCopierFactory.getFieldCopier(field, policy.getFieldAction(field)))
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
