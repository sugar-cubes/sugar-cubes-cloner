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

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.ObjectCopier;
import io.github.sugarcubes.cloner.spi.FieldAdapter;

/**
 * Copier provider interface.
 *
 * @author Maxim Butov
 */
public interface CopierProvider {

    /**
     * Returns copier for the object.
     *
     * @param <T> object type
     * @param original original object
     * @return copier
     */
    <T> ObjectCopier<T> getCopier(T original);

    /**
     * Returns the adapter of a field, for the holes left by copy constructors.
     *
     * @param declaringClass class declaring the field
     * @param field name of the field
     * @return field adapter
     * @throws ClonerException if the field cannot be accessed
     */
    default FieldAdapter fieldAdapter(Class<?> declaringClass, String field) {
        throw new ClonerException(String.format("%s does not support copy constructors.", getClass().getName()));
    }

}
