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

import java.util.ArrayList;
import java.util.List;

import io.github.sugarcubes.cloner.platform.JdkPlatform;
import io.github.sugarcubes.cloner.unsafe.UnsafeBridge;
import io.github.sugarcubes.cloner.util.ClassUtils;
import io.github.sugarcubes.cloner.util.Lazy;

/**
 * Chooses the fastest mechanisms which the running JVM allows, for {@link Toolsets#defaults()}. The JVM is probed once,
 * the choice is shared by all default toolsets.
 *
 * <p>Order of preference, by speed of cloning and by coverage:</p>
 * <ul>
 *     <li>field access: {@code Unsafe} if its memory access is not denied ({@code --sun-misc-unsafe-memory-access}),
 *     then reflection, then {@code VarHandle} of the trusted lookup if {@code Unsafe} cannot be used;</li>
 *     <li>allocation: {@code Unsafe}, then {@code ReflectionFactory};</li>
 *     <li>module opening (JDK 9+): the cloner agent, then {@code Unsafe}, then Narcissus; {@code Unsafe} goes before
 *     Narcissus because it is already used for fields and adds no warnings of the JVM.</li>
 * </ul>
 *
 * <p>Narcissus is not used for field access: it is an order of magnitude slower than the other mechanisms.</p>
 *
 * @author Maxim Butov
 */
final class ToolsetDetection {

    /**
     * Mechanisms chosen for the running JVM.
     */
    private static final class Choice {

        final ModuleOpening moduleOpening;

        final Allocation[] allocation;

        final FieldAccess[] fieldAccess;

        Choice(ModuleOpening moduleOpening, List<Allocation> allocation, List<FieldAccess> fieldAccess) {
            this.moduleOpening = moduleOpening;
            this.allocation = allocation.toArray(new Allocation[0]);
            this.fieldAccess = fieldAccess.toArray(new FieldAccess[0]);
        }

    }

    /**
     * Field read by the probe of {@code Unsafe} memory access.
     */
    private static final class Probe {

        @SuppressWarnings("unused")
        int value;

    }

    private static final Lazy<Choice> CHOICE = new Lazy<>(ToolsetDetection::detect);

    /**
     * Creates a builder with the mechanisms chosen for the running JVM.
     *
     * @return toolset builder
     */
    static ToolsetBuilder builder() {
        Choice choice = CHOICE.get();
        return Toolsets.builder()
            .moduleOpening(choice.moduleOpening)
            .allocation(choice.allocation)
            .fieldAccess(choice.fieldAccess);
    }

    private static Choice detect() {
        boolean unsafe = isAvailable(() -> unsafeBridge());
        boolean memoryAccess = unsafe && isAvailable(() -> unsafeBridge().objectFieldOffset(
            Probe.class.getDeclaredField("value")));

        List<Allocation> allocation = new ArrayList<>();
        if (unsafe) {
            allocation.add(Allocation.UNSAFE);
        }
        if (Toolsets.isAvailable(Allocation.REFLECTION_FACTORY)) {
            allocation.add(Allocation.REFLECTION_FACTORY);
        }

        List<FieldAccess> fieldAccess = new ArrayList<>();
        if (memoryAccess) {
            fieldAccess.add(FieldAccess.UNSAFE);
        }
        fieldAccess.add(FieldAccess.REFLECTION);
        if (!memoryAccess && isAvailable(() -> Toolsets.builder().fieldAccess(FieldAccess.VAR_HANDLE).build())) {
            fieldAccess.add(FieldAccess.VAR_HANDLE);
        }

        return new Choice(moduleOpening(memoryAccess), allocation, fieldAccess);
    }

    private static ModuleOpening moduleOpening(boolean memoryAccess) {
        if (!ClassUtils.isClassAvailable("java.lang.Module")) {
            // JDK 8, no modules
            return ModuleOpening.NONE;
        }
        List<ModuleOpening> candidates = new ArrayList<>();
        candidates.add(ModuleOpening.INSTRUMENTATION);
        if (memoryAccess) {
            candidates.add(ModuleOpening.UNSAFE);
        }
        candidates.add(ModuleOpening.NARCISSUS);
        for (ModuleOpening candidate : candidates) {
            if (isAvailable(() -> Toolsets.builder().moduleOpening(candidate).build())) {
                return candidate;
            }
        }
        return ModuleOpening.NONE;
    }

    private static UnsafeBridge unsafeBridge() {
        return JdkPlatform.current().lowLevelAccess().unsafeBridge();
    }

    /**
     * Probe of a mechanism.
     */
    @FunctionalInterface
    private interface Check {

        void run() throws ReflectiveOperationException;

    }

    private static boolean isAvailable(Check check) {
        try {
            check.run();
            return true;
        }
        catch (ClonerException | LinkageError | ReflectiveOperationException | UnsupportedOperationException e) {
            // not available: a module is missing or the JVM denies the mechanism
            return false;
        }
    }

    private ToolsetDetection() {
    }

}
