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
package io.github.sugarcubes.cloner;

import java.lang.reflect.Method;

/**
 * One-phase copier which creates a shallow clone of the original object with {@link Object#clone()} method.
 *
 * @author Maxim Butov
 */
public class CloneableCopier<T extends Cloneable> implements ObjectCopier<T> {

    /**
     * {@link Object#clone()} method.
     */
    private static final Method CLONE_METHOD = ReflectionUtils.getMethod(Object.class, "clone");

    @Override
    public T copy(T original, CopyContext context) throws Exception {
        T clone = (T) CLONE_METHOD.invoke(original);
        context.register(clone);
        return clone;
    }

}
