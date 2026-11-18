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
package io.github.sugarcubes.cloner.opening;

import org.junit.jupiter.api.Assertions;

import io.github.sugarcubes.cloner.Cloners;
import io.github.sugarcubes.cloner.Toolset;

/**
 * Checks for tests run without {@code --add-opens} options. Module opening is global, so every test class runs
 * in its own JVM.
 */
final class OpeningTesting {

    static void assertNoAddOpens() {
        Assertions.assertFalse(Object.class.getModule().isOpen("java.lang", OpeningTesting.class.getModule()),
            "the test must run without --add-opens");
    }

    /**
     * Clones a JDK class without a model: its private fields are accessible only if the toolset opens java.lang.
     */
    static void assertClones(Toolset toolset) {
        StringBuilder original = new StringBuilder("abc");

        StringBuilder clone = Cloners.builder().toolset(toolset).build().clone(original);

        Assertions.assertNotSame(original, clone);
        Assertions.assertEquals("abc", clone.toString());
    }

    private OpeningTesting() {
    }

}
