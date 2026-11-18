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
 * Mechanisms reading and writing fields, see {@link ToolsetBuilder#fieldAccess(FieldAccess...)}.
 *
 * @author Maxim Butov
 */
public enum FieldAccess {

    /**
     * Reflection. Needs the package of the field opened to the cloner on JDK 9+ (or a {@link ModuleOpening}).
     */
    REFLECTION,

    /**
     * {@code Unsafe}, works for any package. Since JDK 24 the JVM warns on the first use of {@code sun.misc.Unsafe}
     * memory access.
     */
    UNSAFE,

    /**
     * {@code VarHandle} of the trusted lookup, JDK 9+. Needs {@code java.base/java.lang.invoke} opened to the cloner.
     */
    VAR_HANDLE,

    /**
     * Narcissus library with its native part, must be on the classpath.
     * Narcissus loads a native library: since JDK 24 the JVM warns about it unless started with
     * {@code --enable-native-access=ALL-UNNAMED} (or the module of Narcissus), a future JDK will deny it.
     */
    NARCISSUS,

}
