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
 * {@link TypeModels} of JDK 8: containers.
 *
 * @author Maxim Butov
 */
class TypeModels8 implements TypeModels {

    private static final ContainerModels CONTAINER_MODELS = new ContainerModels();

    @Override
    public <T> Optional<ContainerModel<T>> containerModel(Class<T> type) {
        return CONTAINER_MODELS.containerModel(type);
    }

    @Override
    public <T> Optional<ValuesModel<T>> valuesModel(Class<T> type, Reflection reflection) {
        return Optional.empty();
    }

}
