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
 * Profiles of {@link Toolset}: ready combinations of the low-level mechanisms.
 *
 * @author Maxim Butov
 */
public final class Toolsets {

    /**
     * Default toolset: reflection, with {@code Unsafe} only for fields which reflection cannot access (packages not
     * opened to the cloner); instances are created with Objenesis if it is available, constructors are the fallback.
     * Since JDK 24 the JVM warns when {@code Unsafe} is used for the first time.
     *
     * @return default toolset
     */
    public static Toolset defaults() {
        return builder().build();
    }

    /**
     * Reflection only, never {@code Unsafe}: fields of packages not opened to the cloner cannot be copied.
     *
     * @return reflection toolset
     */
    public static Toolset reflection() {
        return builder().fieldAccess(FieldAccess.REFLECTION).build();
    }

    /**
     * {@code Unsafe} for creating instances and accessing fields: works for any package. Since JDK 24 the JVM warns when
     * {@code Unsafe} is used for the first time.
     *
     * @return Unsafe toolset
     */
    public static Toolset unsafe() {
        return builder().allocation(Allocation.UNSAFE).fieldAccess(FieldAccess.UNSAFE).build();
    }

    /**
     * Narcissus library for accessing fields: works for any package. Narcissus must be on the classpath.
     *
     * @return Narcissus toolset
     */
    public static Toolset narcissus() {
        return builder().fieldAccess(FieldAccess.NARCISSUS).build();
    }

    /**
     * Creates a builder for a custom toolset.
     *
     * @return toolset builder
     */
    public static ToolsetBuilder builder() {
        return new ToolsetBuilder();
    }

    private Toolsets() {
    }

}
