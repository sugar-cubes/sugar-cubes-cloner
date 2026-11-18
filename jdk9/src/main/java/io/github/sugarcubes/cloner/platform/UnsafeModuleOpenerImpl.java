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
 * Opens packages to the cloner by the package-private {@code Module.implAddOpens(String, Module)} method (the one
 * behind {@code --add-opens}, present since JDK 9), which is called through the trusted lookup
 * {@code MethodHandles.Lookup.IMPL_LOOKUP} read by {@code Unsafe}.
 */
class UnsafeModuleOpenerImpl extends AbstractModuleOpener {

    private final MethodHandle implAddOpens;

    UnsafeModuleOpenerImpl(UnsafeBridge unsafe) {
        this.implAddOpens = ClonerExceptionUtils.replaceException(() -> {
            MethodHandles.Lookup lookup = TrustedLookup.byUnsafe(unsafe);
            MethodType type = MethodType.methodType(void.class, String.class, Module.class);
            return lookup.findVirtual(Module.class, "implAddOpens", type);
        });
    }

    @Override
    protected void openModule(Module module, String packageName, Module thisModule) throws Throwable {
        implAddOpens.invokeExact(module, packageName, thisModule);
    }

}
