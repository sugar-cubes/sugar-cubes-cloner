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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GenerationTargetsTest {

    @Test
    void masksOfAllResourcesAreCombined(@TempDir Path dir) throws IOException {
        URL[] roots = {root(dir.resolve("a"), "com.acme.**   # own classes\n\n!com.acme.cache.**\n"),
            root(dir.resolve("b"), "org.lib.money.Amount\n")};
        ClassLoader loader = new java.net.URLClassLoader(roots, null);

        ClassNameFilter filter = GenerationTargets.read(loader);

        Assertions.assertTrue(filter.matches("com.acme.Order"));
        Assertions.assertTrue(filter.matches("org.lib.money.Amount"));
        Assertions.assertFalse(filter.matches("com.acme.cache.Entry"));
        Assertions.assertFalse(filter.matches("org.lib.money.Currency"));
    }

    @Test
    void noResourcesMatchNothing() {
        ClassLoader loader = new java.net.URLClassLoader(new URL[0], null);

        Assertions.assertFalse(GenerationTargets.read(loader).matches("com.acme.Order"));
        Assertions.assertFalse(GenerationTargets.isTarget(String.class));
    }

    private static URL root(Path root, String content) throws IOException {
        Path file = root.resolve(GenerationTargets.RESOURCE);
        Files.createDirectories(file.getParent());
        Files.write(file, Arrays.asList(content.split("\n", -1)), StandardCharsets.UTF_8);
        return root.toUri().toURL();
    }

}
