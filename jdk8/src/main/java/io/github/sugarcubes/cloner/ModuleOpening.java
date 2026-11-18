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
package io.github.sugarcubes.cloner;

/**
 * Mechanisms opening packages of other modules to the cloner for reflection, JDK 9+; on JDK 8 there is nothing to open.
 * See {@link ToolsetBuilder#moduleOpening(ModuleOpening)}.
 *
 * @author Maxim Butov
 */
public enum ModuleOpening {

    /**
     * Packages are not opened, only those opened by the JVM options ({@code --add-opens}) are accessible.
     */
    NONE,

    /**
     * Opens packages with {@code Unsafe}. Changes the module of an internal class of the cloner, use with care.
     */
    UNSAFE,

    /**
     * Opens packages with the Narcissus library, which must be on the classpath.
     * Narcissus loads a native library: since JDK 24 the JVM warns about it unless started with
     * {@code --enable-native-access=ALL-UNNAMED} (or the module of Narcissus), a future JDK will deny it.
     */
    NARCISSUS,

    /**
     * Opens packages with {@code Instrumentation} of the cloner agent, the JVM must be started with
     * {@code -javaagent} pointing to the cloner jar.
     */
    INSTRUMENTATION,

}
