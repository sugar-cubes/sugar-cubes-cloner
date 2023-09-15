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
package io.github.sugarcubes.cloner;

/**
 * Two-phase copier.
 *
 * @author Maxim Butov
 */
public abstract class TwoPhaseObjectCopier<T> implements ObjectCopier<T>, ObjectAllocator<T> {

    @Override
    public T copy(T original, CopyContext context) throws Exception {
        T clone = allocate(original);
        context.register(clone);
        context.thenInvoke(
            () -> {
                deepCopy(original, clone, context);
                return null;
            }
        );
        return clone;
    }

    /**
     * The first phase of copying. Creates an "empty" copy of the original object.
     *
     * @param original original object
     * @return clone
     * @throws Exception if something went wrong
     */
    @Override
    public abstract T allocate(T original) throws Exception;

    /**
     * The second phase of copying, copies the inner state from the original object into the clone.
     *
     * @param original original object
     * @param clone clone
     * @param context copying context
     * @throws Exception if something went wrong
     */
    public abstract void deepCopy(T original, T clone, CopyContext context) throws Exception;

}
