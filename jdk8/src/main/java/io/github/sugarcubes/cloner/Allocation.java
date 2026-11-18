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
 * Mechanisms creating instances without calling constructors, see {@link ToolsetBuilder#allocation(Allocation...)}.
 *
 * @author Maxim Butov
 */
public enum Allocation {

    /**
     * Objenesis library, must be on the classpath.
     */
    OBJENESIS,

    /**
     * {@code Unsafe.allocateInstance()}. Since JDK 24 the JVM warns on the first use of {@code sun.misc.Unsafe}
     * memory access.
     */
    UNSAFE,

}
