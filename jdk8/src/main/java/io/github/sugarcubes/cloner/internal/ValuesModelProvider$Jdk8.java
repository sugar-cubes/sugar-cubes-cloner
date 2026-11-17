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
package io.github.sugarcubes.cloner.internal;

import java.util.Optional;

import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.spi.ValuesModel;

/**
 * Finds {@link ValuesModel} of a type on JDK 8, which has no records.
 *
 * @author Maxim Butov
 */
class ValuesModelProvider$Jdk8 {

    /**
     * Returns the values model of the type.
     *
     * @param <T> type
     * @param type type
     * @param reflection reflection to access members of the type
     * @return model or empty if instances of the type are not created from values
     */
    public <T> Optional<ValuesModel<T>> valuesModel(Class<T> type, Reflection reflection) {
        return Optional.empty();
    }

}
