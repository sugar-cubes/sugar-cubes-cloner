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

/**
 * Copier for the specific object type.
 *
 * @author Maxim Butov
 */
public interface ObjectCopier<T> {

    /**
     * Singleton instance of {@link NullCopier}.
     */
    ObjectCopier<?> NULL = new NullCopier<>();

    /**
     * Singleton instance of {@link NoopCopier}.
     */
    ObjectCopier<?> NOOP = new NoopCopier<>();

    /**
     * Singleton instance of {@link ShallowCopier}.
     */
    ObjectCopier<?> SHALLOW = new ShallowCopier<>();

    /**
     * Singleton instance of {@link ObjectArrayCopier}.
     */
    ObjectCopier<?> OBJECT_ARRAY = new ObjectArrayCopier();

    /**
     * Singleton instance of {@link CopyableCopier}.
     */
    ObjectCopier<?> COPYABLE = new CopyableCopier<>();

    /**
     * Creates a copy of the original object.
     * The implementation MUST call {@link CopyContext#register(Object)} method.
     *
     * @param original original object
     * @param context copying context or {@code null} if the copier is trivial
     * @return copy result
     * @throws Exception if something went wrong
     */
    T copy(T original, CopyContext context) throws Exception;

}
