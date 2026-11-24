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

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.sugarcubes.cloner.Cloners;
import io.github.sugarcubes.cloner.ModuleOpening;
import io.github.sugarcubes.cloner.Toolset;
import io.github.sugarcubes.cloner.Toolsets;

/**
 * Default toolset in a JVM which denies {@code Unsafe} memory access, and native access in the strictest run: the
 * expected module opening is given by the {@code expectedOpening} system property.
 */
class RestrictedJvmTest {

    static class Counted {

        static final AtomicInteger CONSTRUCTED = new AtomicInteger();

        private final int value;

        Counted(int value) {
            this.value = value;
            CONSTRUCTED.incrementAndGet();
        }

    }

    @BeforeAll
    static void noAddOpens() {
        OpeningTesting.assertNoAddOpens();
    }

    @Test
    void defaultsAvoidDeniedMechanisms() {
        ModuleOpening opening = ModuleOpening.valueOf(System.getProperty("expectedOpening"));
        Toolset toolset = Toolsets.defaults();

        // allocateInstance is not memory access, the JVM does not deny it
        Assertions.assertEquals(String.format("Toolset[moduleOpening=%s, allocation=[UNSAFE, REFLECTION_FACTORY], "
            + "constructorFallback=constructors, fieldAccess=[REFLECTION]]", opening), toolset.toString());
        Counted original = new Counted(7);
        int constructed = Counted.CONSTRUCTED.get();

        Counted clone = Cloners.clone(original);

        Assertions.assertEquals(7, clone.value);
        Assertions.assertEquals(constructed, Counted.CONSTRUCTED.get());
        if (opening != ModuleOpening.NONE) {
            OpeningTesting.assertClones(toolset);
        }
    }

}
