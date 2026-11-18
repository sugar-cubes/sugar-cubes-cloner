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

/**
 * Type opener which opens the package of a type to the cloner module if it is not open yet.
 */
abstract class AbstractModuleOpener extends AbstractTypeOpener {

    private final Module thisModule = AbstractModuleOpener.class.getModule();

    @Override
    protected void openImpl(Class<?> type) throws Throwable {
        String packageName = type.getPackageName();
        Module module = type.getModule();
        if (!module.isOpen(packageName, thisModule)) {
            openModule(module, packageName, thisModule);
        }
    }

    /**
     * Opens the package of the module to the cloner module.
     *
     * @param module module of the type
     * @param packageName package of the type
     * @param thisModule cloner module
     * @throws Throwable if opening fails
     */
    protected abstract void openModule(Module module, String packageName, Module thisModule) throws Throwable;

}
