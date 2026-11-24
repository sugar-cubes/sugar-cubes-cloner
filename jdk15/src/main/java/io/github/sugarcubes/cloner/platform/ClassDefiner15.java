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
package io.github.sugarcubes.cloner.platform;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;

import io.github.sugarcubes.cloner.generation.BytecodeBackend;
import io.github.sugarcubes.cloner.util.Checks;
import io.github.sugarcubes.cloner.util.ClonerExceptionUtils;

/**
 * {@link ClassDefiner} of JDK 15+: hidden classes in the nest of the host.
 *
 * @author Maxim Butov
 */
class ClassDefiner15 implements ClassDefiner {

    private static final int JAVA_15_CLASS_VERSION = 59;

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int classVersion() {
        return JAVA_15_CLASS_VERSION;
    }

    @Override
    public boolean supportsClassData() {
        return false;
    }

    /**
     * Defines a hidden class in the nest of the host.
     */
    @Override
    public MethodHandle defineNestmate(Class<?> host, byte[] bytes, Class<?> support, MethodType constructor,
        List<?> classData) {
        Module cloner = support.getModule();
        if (!cloner.isExported(support.getPackageName(), host.getModule())) {
            // the caller is in the module of the cloner, so it may export the package
            cloner.addExports(support.getPackageName(), host.getModule());
        }
        return ClonerExceptionUtils.replaceException(() -> {
            MethodHandles.Lookup lookup = defineHidden(fullAccess(host), bytes, classData);
            return lookup.findConstructor(lookup.lookupClass(), constructor);
        });
    }

    /**
     * Defines the hidden class in the nest of the lookup class.
     *
     * @param host lookup with full access to the host
     * @param bytes class file
     * @param classData class data or {@code null}
     * @return lookup of the hidden class
     * @throws IllegalAccessException if the class cannot be defined
     */
    MethodHandles.Lookup defineHidden(MethodHandles.Lookup host, byte[] bytes, List<?> classData)
        throws IllegalAccessException {
        Checks.illegalArg(classData != null, "Class data needs JDK 16+.");
        return host.defineHiddenClass(bytes, true, MethodHandles.Lookup.ClassOption.NESTMATE);
    }

    /**
     * Lookup with full access to the host: a private one if the host is in the module of the cloner (both on the
     * classpath), otherwise the trusted lookup if it is available.
     */
    private MethodHandles.Lookup fullAccess(Class<?> host) throws IllegalAccessException {
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(host, MethodHandles.lookup());
        if (lookup.hasFullPrivilegeAccess()) {
            return lookup;
        }
        try {
            return TrustedLookup.byReflection().in(host);
        }
        catch (LinkageError e) {
            throw new IllegalAccessException(String.format("No full access to %s: open java.base/java.lang.invoke to "
                + "the cloner.", host.getName()));
        }
    }

    @Override
    public Optional<BytecodeBackend> classFileBackend() {
        return Optional.empty();
    }

}
