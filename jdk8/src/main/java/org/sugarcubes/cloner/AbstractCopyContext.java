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

import java.util.Map;
import java.util.function.Supplier;

/**
 * Abstract copy context. Contains common code for the context implementations.
 *
 * @author Maxim Butov
 */
public abstract class AbstractCopyContext implements CopyContext {

    /**
     * Copier provider.
     */
    private final CopierProvider copierProvider;

    /**
     * Previously copied objects.
     */
    private final Map<Object, Object> clones;

    /**
     * Creates context with specified copier provider, clones map and predefined cloned objects.
     *
     * @param copierProvider copier provider
     * @param mapSupplier clones map supplier
     * @param clones predefined cloned objects
     */
    protected AbstractCopyContext(CopierProvider copierProvider, Supplier<Map<?, ?>> mapSupplier, Map<Object, Object> clones) {
        this.copierProvider = copierProvider;
        this.clones = new IdentityMapWrapper<>(mapSupplier, clones);
    }

    /**
     * Integer reference (array of size 1) for cloning/registration counter.
     *
     * @return counter
     */
    protected abstract int[] counter();

    @Override
    public <T> void register(T original, T clone) {
        if (--counter()[0] < 0) {
            registrationMismatch();
        }
        if (clones.put(original, clone) != null) {
            registrationMismatch();
        }
    }

    /**
     * Throws exception with message describing registration error.
     */
    protected void registrationMismatch() {
        throw new ClonerException("Registration mismatch. " +
            "If you use custom ObjectCopier or Copyable objects, " +
            "ensure that you call CopyContext.register(original, clone) method " +
            "and call it exactly once.");
    }

    @Override
    public <T> T copy(T original) throws Exception {
        if (original == null) {
            return null;
        }

        ObjectCopier<T> copier = copierProvider.getCopier(original);

        // trivial cases
        if (copier == ObjectCopier.NOOP) {
            return original;
        }
        if (copier == ObjectCopier.NULL) {
            return null;
        }

        // non-trivial case
        return doClone(original, copier);
    }

    /**
     * Complex copying which must return non-null and non-original object.
     *
     * @param <T> object type
     * @param original original object
     * @param copier object copier
     * @return copy of the original object
     * @throws Exception if something went wrong
     */
    protected <T> T doClone(T original, ObjectCopier<T> copier) throws Exception {
        T clone = (T) clones.get(original);
        if (clone != null) {
            return clone;
        }
        int[] counter = counter();
        int count = counter[0]++;
        clone = copier.copy(original, this);
        if (count != counter[0]) {
            registrationMismatch();
        }
        return clone;
    }

    /**
     * Completes all the delayed tasks.
     *
     * @throws Throwable if something went wrong
     */
    public abstract void complete() throws Throwable;

}
