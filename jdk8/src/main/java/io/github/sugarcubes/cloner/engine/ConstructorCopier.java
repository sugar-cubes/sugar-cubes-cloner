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

import java.lang.invoke.MethodHandle;

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.CopyContext;
import io.github.sugarcubes.cloner.ObjectCopier;
import io.github.sugarcubes.cloner.generation.CopyConstructors;

/**
 * Copier which creates the clone by the copy constructor of the class, see {@link CopyConstructors}. The constructor
 * registers the clone before copying the fields, the context runs it as the second phase of the clone: holes of
 * reference cycles are filled later.
 *
 * @param <T> object type
 * @author Maxim Butov
 */
final class ConstructorCopier<T> implements ObjectCopier<T> {

    /**
     * Copy constructor of type {@code (CopyConstructors, Object, CopyContext)Object}.
     */
    private final MethodHandle constructor;

    /**
     * Creates the copier.
     *
     * @param constructor copy constructor of type {@code (CopyConstructors, Object, CopyContext)Object}
     */
    ConstructorCopier(MethodHandle constructor) {
        this.constructor = constructor;
    }

    @Override
    public T copy(T original, CopyContext context) throws Exception {
        return construct(original, context);
    }

    /**
     * Calls the copy constructor.
     *
     * @param original original object
     * @param context context which registers the clone
     * @return clone
     * @throws Exception if copying fails
     */
    @SuppressWarnings("unchecked")
    T construct(T original, CopyContext context) throws Exception {
        try {
            return (T) (Object) constructor.invokeExact((CopyConstructors) null, (Object) original, context);
        }
        catch (Exception | Error e) {
            throw e;
        }
        catch (Throwable e) {
            throw new ClonerException(e);
        }
    }

}
