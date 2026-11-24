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
package io.github.sugarcubes.cloner.tools;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;

import io.github.sugarcubes.cloner.Cloners;
import io.github.sugarcubes.cloner.CopyPolicy;
import io.github.sugarcubes.cloner.Toolsets;
import io.github.sugarcubes.cloner.engine.ReflectionCopierProvider;
import io.github.sugarcubes.cloner.generation.GenerationTargets;
import io.github.sugarcubes.cloner.tools.samples.Order;

/**
 * Sample classes: their class files, sources and a class loader of enhanced copies.
 */
final class Samples {

    static final String PACKAGE = "io.github.sugarcubes.cloner.tools.samples";

    static final String PATH = PACKAGE.replace('.', '/');

    static final List<String> MASKS = Arrays.asList(PACKAGE + ".**", "!" + PACKAGE + ".NotListed");

    /**
     * Classes which get copy constructors.
     */
    static final List<String> ENHANCED = Arrays.asList("Order", "Line", "DiscountLine", "Customer", "Node");

    /**
     * Listed classes whose superclasses have no copy constructors.
     */
    static final List<String> SKIPPED = Arrays.asList("ExtendsNotListed", "ExtendsArrayList");

    private Samples() {
    }

    static Path sources() {
        return Paths.get(System.getProperty("samples.sources"));
    }

    static void copyClasses(Path target) throws IOException, URISyntaxException {
        Path classes = Paths.get(Order.class.getProtectionDomain().getCodeSource().getLocation().toURI()).resolve(PATH);
        Path dir = Files.createDirectories(target.resolve(PATH));
        try (Stream<Path> files = Files.list(classes)) {
            files.forEach(file -> copy(file, dir.resolve(file.getFileName().toString())));
        }
    }

    static void writeMasks(Path root) throws IOException {
        Path file = root.resolve(GenerationTargets.RESOURCE);
        Files.createDirectories(file.getParent());
        Files.write(file, MASKS);
    }

    private static void copy(Path from, Path to) {
        try {
            Files.copy(from, to);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static byte[] classFile(Path root, String simpleName) throws IOException {
        return Files.readAllBytes(root.resolve(PATH + "/" + simpleName + ".class"));
    }

    static void assertEnhanced(Path root) throws IOException {
        for (String name : ENHANCED) {
            Assertions.assertTrue(ClassInfo.read(classFile(root, name)).hasCopyConstructor(), name);
        }
        for (String name : SKIPPED) {
            Assertions.assertFalse(ClassInfo.read(classFile(root, name)).hasCopyConstructor(), name);
        }
        Assertions.assertFalse(ClassInfo.read(classFile(root, "NotListed")).hasCopyConstructor());
    }

    /**
     * Loads the samples from the directory, other classes from the class loader of the test.
     */
    static ClassLoader loader(Path root) throws MalformedURLException {
        return new URLClassLoader(new URL[] {root.toUri().toURL()}, Samples.class.getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (!name.startsWith(PACKAGE + ".")) {
                    return super.loadClass(name, resolve);
                }
                synchronized (getClassLoadingLock(name)) {
                    Class<?> loaded = findLoadedClass(name);
                    return loaded != null ? loaded : findClass(name);
                }
            }
        };
    }

    /**
     * Clones the samples of the enhanced classes: by their copy constructors, correctly.
     */
    static void assertClonedByCopyConstructors(Path root) throws Exception {
        ClassLoader loader = loader(root);
        ReflectionCopierProvider provider = new ReflectionCopierProvider(Toolsets.defaults(), null,
            CopyPolicy.defaultPolicy(), CopyPolicy.defaultPolicy(), Collections.emptyMap(), Collections.emptySet(),
            true);
        for (String name : Arrays.asList("Order", "Node")) {
            Object original = loader.loadClass(PACKAGE + "." + name).getMethod("sample").invoke(null);

            Object clone = Cloners.clone(original);

            Assertions.assertEquals("ConstructorCopier", provider.getCopier(original).getClass().getSimpleName());
            Assertions.assertNotSame(original, clone);
            Assertions.assertEquals(original.toString(), clone.toString());
        }
    }

}
