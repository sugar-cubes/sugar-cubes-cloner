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

import java.lang.reflect.Method;

import io.github.sugarcubes.cloner.util.ClonerExceptionUtils;
import io.github.toolfactory.narcissus.Narcissus;

/**
 * Opens packages to the cloner by the package-private {@code Module.implAddOpens(String, Module)} method called with
 * Narcissus (JNI), which ignores access checks.
 */
class NarcissusModuleOpenerImpl extends AbstractModuleOpener {

    private final Method implAddOpens;

    NarcissusModuleOpenerImpl() {
        // the method behind --add-opens, present since JDK 9
        implAddOpens = ClonerExceptionUtils.replaceException(() ->
            Narcissus.findMethod(Module.class, "implAddOpens", String.class, Module.class));
    }

    @Override
    protected void openModule(Module module, String packageName, Module thisModule) throws Exception {
        Narcissus.invokeVoidMethod(module, implAddOpens, packageName, thisModule);
    }

}
