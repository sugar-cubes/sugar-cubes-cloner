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
package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Field copy policy based on annotations.
 *
 * @author Maxim Butov
 */
public class AnnotatedFieldCopyPolicy implements CopyPolicy<Field> {

    @Override
    public CopyAction getAction(Field field) {
        return Stream.of(field, field.getDeclaringClass())
            .map(ae -> ae.getDeclaredAnnotation(FieldPolicy.class))
            .filter(Objects::nonNull)
            .map(FieldPolicy::value)
            .findFirst()
            .orElse(CopyAction.DEFAULT);
    }

}
