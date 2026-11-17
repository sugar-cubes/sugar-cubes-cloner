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
package io.github.sugarcubes.cloner.spi;

/**
 * Low-level model of a type whose instances are created from the values of their components, e.g. a record. Knows how
 * to read the values and to create an instance from them; what and how to copy is decided by the caller.
 *
 * @param <T> type
 * @author Maxim Butov
 */
public interface ValuesModel<T> {

    /**
     * Returns the type.
     *
     * @return type
     */
    Class<T> getType();

    /**
     * Returns the names of the components, in the order of values.
     *
     * @return names of the components
     */
    String[] getNames();

    /**
     * Reads the values of the components.
     *
     * @param object object
     * @return values of the components
     */
    Object[] read(T object);

    /**
     * Creates an instance from the values of the components. If the constructor fails, throws
     * {@link io.github.sugarcubes.cloner.ClonerException} with the exception of the constructor as the cause.
     *
     * @param values values of the components
     * @return new instance
     */
    T create(Object[] values);

}
