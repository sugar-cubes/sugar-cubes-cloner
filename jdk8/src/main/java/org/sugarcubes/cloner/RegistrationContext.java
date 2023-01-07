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

import java.util.concurrent.Callable;

/**
 * A copy context which registers clone in the cache.
 *
 * @author Maxim Butov
 */
public final class RegistrationContext implements CopyContext {

    /**
     * Initial copy context.
     */
    private final CopyContext context;

    /**
     * Original object.
     */
    private final Object original;

    /**
     * Cached clone reference.
     */
    private final Object[] cached;

    /**
     * Creates registration context wrapper.
     *
     * @param context initial context
     * @param original original
     * @param cached cached clone reference
     */
    public RegistrationContext(CopyContext context, Object original, Object[] cached) {
        this.context = context;
        this.original = original;
        this.cached = cached;
    }

    /**
     * Throws exception with message describing registration error.
     */
    private void registrationMismatch() {
        throw new ClonerException(String.format("Registration mismatch when cloning '%s'. " +
            "If you use custom ObjectCopier or Copyable objects, " +
            "ensure that you call CopyContext.register(clone) method " +
            "and call it exactly once.", original));
    }

    @Override
    public <T> T register(T clone) {
        if (clone == null || cached[0] != null) {
            registrationMismatch();
        }
        cached[0] = clone;
        return clone;
    }

    @Override
    public <T> T copy(T original) throws Exception {
        return context.copy(original);
    }

    @Override
    public void thenInvoke(Callable<?> task) throws Exception {
        context.thenInvoke(task);
    }

    /**
     * Checks that the cached clone is the same as returned from {@link #copy(Object)} method.
     *
     * @param clone clone
     */
    public void check(Object clone) {
        if (cached[0] != clone) {
            registrationMismatch();
        }
    }

}
