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
package io.github.sugarcubes.cloner.opening;

import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.sugarcubes.cloner.Allocation;
import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.Cloners;
import io.github.sugarcubes.cloner.FieldAccess;
import io.github.sugarcubes.cloner.ModuleOpening;
import io.github.sugarcubes.cloner.Toolset;
import io.github.sugarcubes.cloner.Toolsets;
import io.github.sugarcubes.cloner.generation.CopierGenerator;

/**
 * Mechanisms which do not open modules.
 */
class NoOpeningTest {

    @BeforeAll
    static void noAddOpens() {
        OpeningTesting.assertNoAddOpens();
    }

    @Test
    void reflectionAloneCannotAccessJdkInternals() {
        Toolset toolset = Toolsets.builder().fieldAccess(FieldAccess.REFLECTION).build();

        ClonerException error = Assertions.assertThrows(ClonerException.class,
            () -> Cloners.builder().toolset(toolset).build().clone(new StringBuilder("abc")));

        Assertions.assertTrue(error.getMessage().contains("--add-opens"), error.getMessage());
    }

    @Test
    void defaults() {
        Assertions.assertTrue(Toolsets.isAvailable(Allocation.REFLECTION_FACTORY));
        // Unsafe opens modules before Narcissus, which is on the classpath
        Assertions.assertEquals("Toolset[moduleOpening=UNSAFE, allocation=[UNSAFE, REFLECTION_FACTORY], "
            + "constructorFallback=constructors, fieldAccess=[UNSAFE, REFLECTION]]", Toolsets.defaults().toString());
        OpeningTesting.assertClones(Toolsets.defaults());
        // TreeMap entries have no constructor without arguments
        TreeMap<String, Integer> original = new TreeMap<>(Map.of("a", 1, "b", 2));

        Assertions.assertEquals(original, Cloners.clone(original));
    }

    @Test
    void noCopierGenerationWithoutAsm() {
        // JDK 17 without ASM on the classpath: no backend, no failure
        Assertions.assertFalse(CopierGenerator.instance().isPresent());
    }

    @Test
    void narcissusFieldAccess() {
        Assertions.assertTrue(Toolsets.isAvailable(FieldAccess.NARCISSUS));
        OpeningTesting.assertClones(Toolsets.narcissus());
    }

    @Test
    void varHandleNeedsOpening() {
        Assertions.assertFalse(Toolsets.isAvailable(FieldAccess.VAR_HANDLE));
    }

    @Test
    void instrumentationNeedsAgent() {
        Assertions.assertFalse(Toolsets.isAvailable(ModuleOpening.INSTRUMENTATION));
    }

}
