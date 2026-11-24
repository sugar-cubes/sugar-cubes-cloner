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
package io.github.sugarcubes.cloner.generation;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.sugarcubes.cloner.ClonerException;

class ClassNameFilterTest {

    @Test
    void singleStarStaysInPackage() {
        ClassNameFilter filter = ClassNameFilter.of(Arrays.asList("com.acme.*"));

        Assertions.assertTrue(filter.matches("com.acme.Order"));
        Assertions.assertTrue(filter.matches("com.acme.Order$Line"));
        Assertions.assertFalse(filter.matches("com.acme.model.Order"));
        Assertions.assertFalse(filter.matches("com.acmex.Order"));
    }

    @Test
    void doubleStarIncludesSubpackages() {
        ClassNameFilter filter = ClassNameFilter.of(Arrays.asList("com.acme.**"));

        Assertions.assertTrue(filter.matches("com.acme.Order"));
        Assertions.assertTrue(filter.matches("com.acme.model.deep.Order"));
        Assertions.assertFalse(filter.matches("org.acme.Order"));
    }

    @Test
    void questionMarkIsOneCharacter() {
        ClassNameFilter filter = ClassNameFilter.of(Arrays.asList("com.acme.Order?"));

        Assertions.assertTrue(filter.matches("com.acme.Order1"));
        Assertions.assertFalse(filter.matches("com.acme.Order"));
        Assertions.assertFalse(filter.matches("com.acme.Order12"));
    }

    @Test
    void exclusionsWinRegardlessOfOrder() {
        ClassNameFilter filter = ClassNameFilter.of(Arrays.asList("!com.acme.cache.**", "com.acme.**"));

        Assertions.assertTrue(filter.matches("com.acme.Order"));
        Assertions.assertFalse(filter.matches("com.acme.cache.Entry"));
    }

    @Test
    void exactNames() {
        ClassNameFilter filter = ClassNameFilter.of(Arrays.asList("org.lib.money.Amount"));

        Assertions.assertTrue(filter.matches("org.lib.money.Amount"));
        Assertions.assertFalse(filter.matches("org.lib.money.AmountX"));
        Assertions.assertFalse(ClassNameFilter.NONE.matches("org.lib.money.Amount"));
    }

    @Test
    void invalidMasks() {
        Assertions.assertThrows(ClonerException.class, () -> ClassNameFilter.of(Arrays.asList("com.acme/Order")));
        Assertions.assertThrows(ClonerException.class, () -> ClassNameFilter.of(Arrays.asList("!")));
    }

}
