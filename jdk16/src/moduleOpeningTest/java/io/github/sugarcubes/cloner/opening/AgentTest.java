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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.sugarcubes.cloner.FieldAccess;
import io.github.sugarcubes.cloner.ModuleOpening;
import io.github.sugarcubes.cloner.Toolsets;

/**
 * Module opening by the cloner agent, run with {@code -javaagent} pointing to the cloner jar.
 */
class AgentTest {

    @BeforeAll
    static void noAddOpens() {
        OpeningTesting.assertNoAddOpens();
    }

    @Test
    void instrumentationOpening() {
        Assertions.assertTrue(Toolsets.isAvailable(ModuleOpening.INSTRUMENTATION));
        OpeningTesting.assertClones(Toolsets.builder()
            .moduleOpening(ModuleOpening.INSTRUMENTATION)
            .fieldAccess(FieldAccess.REFLECTION)
            .build());
    }

    @Test
    void defaultsPreferAgent() {
        Assertions.assertTrue(Toolsets.defaults().toString().startsWith("Toolset[moduleOpening=INSTRUMENTATION,"),
            Toolsets.defaults().toString());
        OpeningTesting.assertClones(Toolsets.defaults());
    }

}
