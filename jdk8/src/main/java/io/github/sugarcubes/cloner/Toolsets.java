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
     * Default toolset: the fastest mechanisms which the running JVM allows, chosen once per JVM. Fields are accessed
     * by {@code Unsafe} unless its memory access is denied, by reflection otherwise; instances are created by
     * {@code Unsafe} or {@code sun.reflect.ReflectionFactory} without constructors, constructors are the last fallback;
     * packages are opened by the cloner agent, {@code Unsafe} or Narcissus, whichever is available. Since JDK 24 the JVM
     * warns when {@code Unsafe} accesses a field for the first time. {@link Toolset#toString()} tells the choice.
     *
     * <p>{@link #conservative()} is the fixed choice which does not depend on the JVM.</p>
     *
     * @return default toolset
     */
    public static Toolset defaults() {
        return ToolsetDetection.builder().build();
    }

    /**
     * Conservative toolset, the same for every JVM: reflection, with {@code Unsafe} only for fields which reflection
     * cannot access (packages not opened to the cloner); instances are created without constructors by
     * {@code sun.reflect.ReflectionFactory}, or by {@code Unsafe} if the runtime has no {@code jdk.unsupported} module,
     * constructors are the last fallback; packages are not opened. The same as {@code Toolsets.builder().build()}.
     *
     * @return conservative toolset
     */
    public static Toolset conservative() {
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

    /**
     * Tells whether the instance allocation mechanism is available in the running JVM.
     *
     * @param allocation mechanism
     * @return {@code true} if the mechanism can be used
     */
    public static boolean isAvailable(Allocation allocation) {
        return canBuild(builder().allocation(allocation));
    }

    /**
     * Tells whether the field access mechanism is available in the running JVM.
     *
     * @param fieldAccess mechanism
     * @return {@code true} if the mechanism can be used
     */
    public static boolean isAvailable(FieldAccess fieldAccess) {
        return canBuild(builder().fieldAccess(fieldAccess));
    }

    /**
     * Tells whether the module opening mechanism is available in the running JVM.
     *
     * @param moduleOpening mechanism
     * @return {@code true} if the mechanism can be used
     */
    public static boolean isAvailable(ModuleOpening moduleOpening) {
        return canBuild(builder().moduleOpening(moduleOpening));
    }

    private static boolean canBuild(ToolsetBuilder builder) {
        try {
            builder.build();
            return true;
        }
        catch (ClonerException e) {
            return false;
        }
    }

    private Toolsets() {
    }

}
