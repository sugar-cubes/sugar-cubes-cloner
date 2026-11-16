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
     * Invokes task. It can be invoked immediately or later depending on the context implementation.
     * The result of callable is ignored.
     *
     * @param task task
     * @throws Exception if something went wrong
     */
    void thenInvoke(Callable<?> task) throws Exception;

}
