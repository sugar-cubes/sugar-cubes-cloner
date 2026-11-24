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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.spi.ValuesModel;

/**
 * {@link TypeModels} of JDK 16+: records are created from the values of their components, immutable lists may contain
 * nulls ({@link java.util.stream.Stream#toList()}).
 *
 * @author Maxim Butov
 */
class TypeModels16 extends TypeModels9 {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> Optional<ValuesModel<T>> valuesModel(Class<T> type, Reflection reflection) {
        return type.isRecord() ? Optional.of(new RecordValuesModel(type, reflection)) : super.valuesModel(type, reflection);
    }

    @Override
    List<Object> immutableList(Object[] values) {
        // List.of() does not accept nulls, Stream.toList() does
        return Arrays.stream(values).anyMatch(Objects::isNull) ? Arrays.stream(values).toList() : super.immutableList(values);
    }

}
