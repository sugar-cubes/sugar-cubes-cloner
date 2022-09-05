/**
 * Copyright 2017-2022 the original author or authors.
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

import java.util.IdentityHashMap;
import java.util.Map;

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
    private final Map<Object, Object> clones = new IdentityHashMap<>();

    /**
     * Creates context with specified copier provider.
     *
     * @param copierProvider copier provider
     */
    protected AbstractCopyContext(CopierProvider copierProvider) {
        this.copierProvider = copierProvider;
    }

    @Override
    public <T> void register(T original, T clone) {
        clones.put(original, clone);
    }

    @Override
    public <T> T copy(T original) throws Exception {
        if (original == null) {
            return null;
        }
        ObjectCopier<T> copier = copierProvider.getCopier(original);
        // trivial case
        if (copier == ObjectCopier.NULL || copier == ObjectCopier.NOOP) {
            return copier.copy(original, this);
        }
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
        clone = copier.copy(original, this);
        return clone;
    }

    /**
     * Completes all the delayed tasks.
     *
     * @throws Exception if something went wrong
     */
    public abstract void complete() throws Exception;

}
