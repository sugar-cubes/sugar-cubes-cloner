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

import io.github.sugarcubes.cloner.unsafe.UnsafeBridge;
import io.github.sugarcubes.cloner.util.ClonerExceptionUtils;

/**
 * {@link JdkPlatform} of JDK 15+: hidden classes, {@code jdk.internal.misc.Unsafe} with references.
 *
 * @author Maxim Butov
 */
class JdkPlatformImpl$Jdk15 extends JdkPlatformImpl$Jdk9 {

    private static final int JAVA_15_CLASS_VERSION = 59;

    @Override
    public boolean isHidden(Class<?> type) {
        // lambda classes are hidden classes too
        return type.isHidden();
    }

    @Override
    public boolean canDefineNestmates() {
        return true;
    }

    @Override
    public int nestmateClassVersion() {
        return JAVA_15_CLASS_VERSION;
    }

    /**
     * Defines a hidden class in the nest of the host.
     */
    @Override
    public MethodHandle defineNestmate(Class<?> host, byte[] bytes, Class<?> support, MethodType constructor) {
        Module cloner = support.getModule();
        if (!cloner.isExported(support.getPackageName(), host.getModule())) {
            // the caller is in the module of the cloner, so it may export the package
            cloner.addExports(support.getPackageName(), host.getModule());
        }
        return ClonerExceptionUtils.replaceException(() -> {
            MethodHandles.Lookup lookup = fullAccess(host)
                .defineHiddenClass(bytes, true, MethodHandles.Lookup.ClassOption.NESTMATE);
            return lookup.findConstructor(lookup.lookupClass(), constructor);
        });
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
            return trustedLookup().in(host);
        }
        catch (LinkageError e) {
            throw new IllegalAccessException(String.format("No full access to %s: open java.base/java.lang.invoke to "
                + "the cloner.", host.getName()));
        }
    }

    @Override
    UnsafeBridge newJdkInternalMiscUnsafeBridge() {
        return new JdkInternalMiscUnsafeBridge$Jdk15();
    }

}
