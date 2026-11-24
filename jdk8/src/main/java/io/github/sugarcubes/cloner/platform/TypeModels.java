/*
 * Copyright 2017-2026 the original author or authors.
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
package io.github.sugarcubes.cloner.platform;

import java.util.Optional;

import io.github.sugarcubes.cloner.spi.ContainerModel;
import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.spi.ValuesModel;

/**
 * Low-level models of types copied otherwise than by their fields.
 *
 * @author Maxim Butov
 */
public interface TypeModels {

    /**
     * Returns the model of a container of the JDK (collections, maps), rebuilt from its elements.
     *
     * @param <T> type
     * @param type type
     * @return model or empty if the type is no known container
     */
    <T> Optional<ContainerModel<T>> containerModel(Class<T> type);

    /**
     * Returns the model of a type created from values: immutable collections of JDK 9+, records of JDK 16+.
     *
     * @param <T> type
     * @param type type
     * @param reflection reflection to access the components
     * @return model or empty if the type is not created from values
     */
    <T> Optional<ValuesModel<T>> valuesModel(Class<T> type, Reflection reflection);

}
