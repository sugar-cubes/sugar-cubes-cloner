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
package io.github.sugarcubes.cloner.platform;

import io.github.sugarcubes.cloner.util.Checks;
import io.github.sugarcubes.cloner.util.ClonerExceptionUtils;

/**
 * Opens the package of a type to the cloner before {@link ReflectionImpl9} makes its members accessible.
 *
 * @author Maxim Butov
 */
abstract class ModuleOpener {

    /**
     * Opener which opens nothing: members of closed packages stay inaccessible.
     */
    static final ModuleOpener NONE = new ModuleOpener() {

        @Override
        void open(Class<?> type) {
        }

        @Override
        void openPackage(Module module, String packageName, Module cloner) {
            throw Checks.mustNotHappen();
        }

    };

    private static final Module CLONER = ModuleOpener.class.getModule();

    /**
     * Opens the package of the type to the cloner if it is not open yet. Opening is cheap to check, so it is not
     * cached.
     *
     * @param type type
     * @throws io.github.sugarcubes.cloner.ClonerException if opening fails
     */
    void open(Class<?> type) {
        String packageName = type.getPackageName();
        Module module = type.getModule();
        if (!module.isOpen(packageName, CLONER)) {
            ClonerExceptionUtils.replaceException(() -> {
                openPackage(module, packageName, CLONER);
                return null;
            });
        }
    }

    /**
     * Opens the package of the module to the cloner.
     *
     * @param module module of the type
     * @param packageName package of the type
     * @param cloner module of the cloner
     * @throws Throwable if opening fails
     */
    abstract void openPackage(Module module, String packageName, Module cloner) throws Throwable;

}
