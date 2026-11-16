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

import java.util.concurrent.Callable;

/**
 * Context of the single cloning process.
 *
 * @author Maxim Butov
 */
public interface CopyContext {

    /**
     * Receives a clone.
     *
     * @param <T> object type
     */
    @FunctionalInterface
    interface CloneSetter<T> {

        /**
         * Receives the clone, e.g. sets it into a field.
         *
         * @param clone clone
         * @throws Exception if something went wrong
         */
        void set(T clone) throws Exception;

    }

    /**
     * Registers clone in the context.
     *
     * @param <T> object type
     * @param clone clone
     * @return clone
     */
    <T> T register(T clone);

    /**
     * Returns the instant copy of the object. It can be a copy, saved previously into cache, or fresh copy. The copy may not be
     * the full copy of the original object, the inner state can be processed later.
     *
     * @param <T> object type
     * @param original original
     * @return clone
     * @throws Exception if something went wrong
     */
    <T> T copy(T original) throws Exception;

    /**
     * Copies the object and passes the clone to the setter: immediately if the clone exists or can be created now,
     * or later, when the clone of the object being created right now (a reference cycle through a single-phase
     * copier, or another thread) is registered. Until then the object copied by the current second phase is not
     * complete. Outside a second phase of a two-phase copier works as {@link #copy(Object)}.
     *
     * @param <T> object type
     * @param original original
     * @param setter receiver of the clone
     * @throws Exception if something went wrong
     */
    <T> void copy(T original, CloneSetter<? super T> setter) throws Exception;

    /**
     * Returns the copy of the object with its own state already copied (for a two-phase copier the second phase is
     * done, its referenced objects may still be incomplete). Needed by copiers which create the clone from copied
     * values, e.g. canonical constructors of records. If the object is part of a reference cycle with the caller's
     * object, the clone cannot be completed before the caller's clone exists, and {@link ClonerException} is thrown.
     *
     * @param <T> object type
     * @param original original
     * @return clone
     * @throws Exception if something went wrong
     */
    <T> T copyComplete(T original) throws Exception;

    /**
     * Invokes task. It can be invoked immediately or later depending on the context implementation.
     * The result of callable is ignored.
     *
     * @param task task
     * @throws Exception if something went wrong
     */
    void thenInvoke(Callable<?> task) throws Exception;

}
