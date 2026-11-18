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
package io.github.sugarcubes.cloner.platform;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.spi.Reflection;

/**
 * JDK 16+ denies illegal reflective access by default, so not opened packages stay closed.
 */
class ReflectionImplStrongEncapsulationTest {

    @Test
    void notOpenedMemberGivesHint() {
        Reflection reflection = new ReflectionImpl$Jdk9(TypeOpener.NOOP);
        // the test JVM opens only a few java.base packages, java.util.regex is not among them
        Field field = reflection.getDeclaredField(Pattern.class, "pattern");

        ClonerException error = Assertions.assertThrows(ClonerException.class, () -> reflection.makeAccessible(field));

        Assertions.assertTrue(error.getMessage().contains("--add-opens"), error.getMessage());
    }

}
