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
import java.util.stream.Collectors;

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
    private final Map<IdentityReference<Object>, Object[]> clones;

    /**
     * JDK configuration.
     */
    private static final JdkConfiguration JDK_CONFIGURATION = JdkConfigurationHolder.CONFIGURATION;

    /**
     * Predefined system-wide singletons as map.
     */
    @SuppressWarnings("checkstyle:Indentation")
    private static final Map<IdentityReference<Object>, Object[]> SYSTEM_SINGLETONS_AS_MAP =
        JDK_CONFIGURATION.getSystemWideSingletons().stream()
            .collect(Collectors.toMap(IdentityReference::new, singleton -> new Object[] {singleton}));

    /**
     * Creates context with specified copier provider, clones map and predefined cloned objects.
     *
     * @param copierProvider copier provider
     * @param cacheSupplier cache supplier
     * @param clones predefined cloned objects
     */
    protected AbstractCopyContext(CopierProvider copierProvider, Supplier<Map<?, ?>> cacheSupplier, Map<Object, Object> clones) {
        this.copierProvider = copierProvider;
        this.clones = (Map) cacheSupplier.get();
        this.clones.putAll(SYSTEM_SINGLETONS_AS_MAP);
        clones.forEach((key, value) -> this.clones.put(new IdentityReference<>(key), new Object[] {value}));
    }

    @Override
    public <T> T register(T clone) {
        throw new ClonerException("Registration must be called from copy() method.");
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
        Object[] cached = clones.computeIfAbsent(new IdentityReference<>(original), key -> new Object[1]);
        T clone = (T) cached[0];
        if (clone != null) {
            return clone;
        }
        RegistrationContext registration = new RegistrationContext(this, original, cached);
        clone = copier.copy(original, registration);
        if (clone == null) {
            throw new ClonerException(String.format("Non-null clone expected for '%s'.", original));
        }
        registration.check(clone);
        return clone;
    }

    /**
     * Completes all the delayed tasks.
     *
     * @throws Throwable if something went wrong
     */
    public abstract void complete() throws Throwable;

}
