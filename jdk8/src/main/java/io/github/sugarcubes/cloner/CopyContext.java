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
     * Marker returned by {@link #copyNow(Object)} when the clone cannot be got now.
     */
    Object PENDING = new Object() {
        @Override
        public String toString() {
            return "PENDING";
        }
    };

    /**
     * Copies the object if its clone exists or can be created now. Returns {@link #PENDING} if the object is being
     * created right now (a reference cycle through a single-phase copier, or another thread); then the caller
     * {@link #subscribe(Object, CloneSetter) subscribes} to the clone. Outside a second phase of a two-phase copier
     * works as {@link #copy(Object)} and never returns {@link #PENDING}.
     *
     * @param original original
     * @return clone or {@link #PENDING}
     * @throws Exception if something went wrong
     */
    Object copyNow(Object original) throws Exception;

    /**
     * Passes the clone of the object to the setter when it is registered, see {@link #copyNow(Object)}. Until then the
     * object copied by the current second phase is not complete. If the clone is already available, the setter is
     * called at once.
     *
     * @param <T> object type
     * @param original original
     * @param setter receiver of the clone
     * @throws Exception if something went wrong
     */
    <T> void subscribe(T original, CloneSetter<? super T> setter) throws Exception;

    /**
     * Copies the object and passes the clone to the setter, now or later, see {@link #copyNow(Object)}. Convenient,
     * but allocates the setter for every call, copiers on hot paths should use {@link #copyNow(Object)}.
     *
     * @param <T> object type
     * @param original original
     * @param setter receiver of the clone
     * @throws Exception if something went wrong
     */
    @SuppressWarnings("unchecked")
    default <T> void copy(T original, CloneSetter<? super T> setter) throws Exception {
        Object clone = copyNow(original);
        if (clone != PENDING) {
            setter.set((T) clone);
        }
        else {
            subscribe(original, setter);
        }
    }

    /**
     * Returns the copy of the object with its own state already copied (for a two-phase copier the second phase is
     * done, its referenced objects may still be incomplete). Needed by copiers which create the clone from copied
     * values, e.g. canonical constructors of records. If the object is part of a reference cycle with an object being
     * created by the current thread, its clone cannot be completed before that object's clone exists: then the clone
     * is returned incomplete, see {@link #isComplete(Object)}.
     *
     * @param <T> object type
     * @param original original
     * @return clone
     * @throws Exception if something went wrong
     */
    <T> T copyComplete(T original) throws Exception;

    /**
     * Tells whether the clone of the object has its own state copied. A copier which has got an incomplete clone from
     * {@link #copyComplete(Object)} must keep exactly this clone in its own clone: it will be completed after the
     * own clone is registered.
     *
     * @param original original
     * @return {@code true} if the clone is complete or the object needs no copying
     */
    boolean isComplete(Object original);

    /**
     * Invokes task. It can be invoked immediately or later depending on the context implementation.
     * The result of callable is ignored.
     *
     * @param task task
     * @throws Exception if something went wrong
     */
    void thenInvoke(Callable<?> task) throws Exception;

}
