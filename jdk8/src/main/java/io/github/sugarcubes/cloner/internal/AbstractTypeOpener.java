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
package io.github.sugarcubes.cloner.internal;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import io.github.sugarcubes.cloner.ClonerExceptionUtils;

public abstract class AbstractTypeOpener implements TypeOpener {

    private final Set<Class<?>> opened = Collections.newSetFromMap(new WeakHashMap<>());

    @Override
    public <T> Class<T> open(Class<T> type) {
        if (opened.add(type)) {
            ClonerExceptionUtils.replaceException(() -> {
                openImpl(type);
                return null;
            });
        }
        return type;
    }

    protected abstract void openImpl(Class<?> type) throws Exception;

}
