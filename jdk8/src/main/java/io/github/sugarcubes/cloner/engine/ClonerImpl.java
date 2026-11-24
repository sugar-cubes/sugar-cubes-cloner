/*
 * Copyright 2017-2026 the original author or authors.
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

import java.util.function.Supplier;

import io.github.sugarcubes.cloner.Cloner;
import io.github.sugarcubes.cloner.ObjectCopier;
import io.github.sugarcubes.cloner.util.ClonerExceptionUtils;

/**
 * Implementation of {@link Cloner}.
 *
 * @author Maxim Butov
 */
public class ClonerImpl implements Cloner {

    /**
     * Copier provider of the contexts, recognizes trivial originals.
     */
    private final CopierProvider copierProvider;

    /**
     * Creates context for the single copy process.
     */
    private final Supplier<? extends AbstractCopyContext> contextSupplier;

    /**
     * Creates cloner with custom context supplier.
     *
     * @param copierProvider copier provider of the contexts
     * @param contextSupplier context supplier
     */
    public ClonerImpl(CopierProvider copierProvider, Supplier<? extends AbstractCopyContext> contextSupplier) {
        this.copierProvider = copierProvider;
        this.contextSupplier = contextSupplier;
    }

    @Override
    public <T> T clone(T object) {
        // trivial cases need no context
        if (object == null) {
            return null;
        }
        ObjectCopier<T> copier = copierProvider.getCopier(object);
        if (copier == ObjectCopier.NOOP) {
            return object;
        }
        if (copier == ObjectCopier.NULL) {
            return null;
        }
        return ClonerExceptionUtils.replaceException(() -> {
                AbstractCopyContext context = contextSupplier.get();
                T clone = context.copy(object);
                context.complete();
                context.runAfterCopy();
                return clone;
            }
        );
    }

}
