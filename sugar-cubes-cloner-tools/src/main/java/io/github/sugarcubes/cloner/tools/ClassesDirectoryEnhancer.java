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
package io.github.sugarcubes.cloner.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.sugarcubes.cloner.generation.ClassNameFilter;
import io.github.sugarcubes.cloner.generation.GenerationTargets;

/**
 * Adds copy constructors to the classes of a directory after compilation: the common part of the Maven and Gradle
 * plugins. The masks are read from {@value GenerationTargets#RESOURCE} in the directory or given explicitly; other
 * classes of the classpath are read to check superclasses.
 *
 * @author Maxim Butov
 */
public final class ClassesDirectoryEnhancer {

    private static final String CLASS_SUFFIX = ".class";

    private ClassesDirectoryEnhancer() {
    }

    /**
     * Counts of the results.
     */
    public static final class Report {

        private final Map<CopyConstructorEnhancer.Status, Integer> counts = new HashMap<>();

        void add(CopyConstructorEnhancer.Status status) {
            counts.merge(status, 1, Integer::sum);
        }

        /**
         * Returns the number of classes with the status.
         *
         * @param status status
         * @return number of classes
         */
        public int count(CopyConstructorEnhancer.Status status) {
            return counts.getOrDefault(status, 0);
        }

        @Override
        public String toString() {
            return String.format("%d enhanced, %d already enhanced, %d skipped",
                count(CopyConstructorEnhancer.Status.ENHANCED), count(CopyConstructorEnhancer.Status.ALREADY_ENHANCED),
                count(CopyConstructorEnhancer.Status.SKIPPED));
        }

    }

    /**
     * Reads the masks of the resource {@value GenerationTargets#RESOURCE} in the directories.
     *
     * @param roots directories, e.g. classes or resources of a source set
     * @return masks, empty if there is no resource
     */
    public static List<String> readMasks(Collection<File> roots) {
        List<String> masks = new ArrayList<>();
        for (File root : roots) {
            File file = new File(root, GenerationTargets.RESOURCE);
            if (file.isFile()) {
                try {
                    masks.addAll(GenerationTargets.parse(file.toURI().toURL()));
                }
                catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        return masks;
    }

    /**
     * Adds copy constructors to the classes of the directory which match the masks.
     *
     * @param classes directory with class files
     * @param classpath classpath of the compilation, to read superclasses
     * @param masks masks of the classes
     * @param warnings receiver of problems of skipped classes
     * @return report
     * @throws IOException if a file cannot be read or written
     */
    public static Report enhance(Path classes, Collection<File> classpath, List<String> masks, Consumer<String> warnings)
        throws IOException {
        Report report = new Report();
        if (masks.isEmpty() || !Files.isDirectory(classes)) {
            return report;
        }
        List<Path> files;
        try (Stream<Path> walk = Files.walk(classes)) {
            files = walk.filter(file -> file.toString().endsWith(CLASS_SUFFIX) && Files.isRegularFile(file))
                .collect(Collectors.toList());
        }
        Map<String, Path> local = new HashMap<>();
        for (Path file : files) {
            String relative = classes.relativize(file).toString().replace(File.separatorChar, '/');
            local.put(relative.substring(0, relative.length() - CLASS_SUFFIX.length()), file);
        }
        try (URLClassLoader loader = new URLClassLoader(urls(classpath), null)) {
            ClassHierarchy hierarchy = new FilesHierarchy(ClassNameFilter.of(masks), local, loader);
            for (Map.Entry<String, Path> entry : local.entrySet()) {
                byte[] bytes = Files.readAllBytes(entry.getValue());
                CopyConstructorEnhancer.Result result = CopyConstructorEnhancer.enhance(bytes, hierarchy);
                report.add(result.getStatus());
                if (result.getStatus() == CopyConstructorEnhancer.Status.ENHANCED) {
                    Files.write(entry.getValue(), result.getBytes());
                }
                else if (result.getStatus() == CopyConstructorEnhancer.Status.SKIPPED) {
                    warnings.accept(String.format("%s gets no copy constructor: %s",
                        entry.getKey().replace('/', '.'), result.getProblem()));
                }
            }
        }
        return report;
    }

    private static URL[] urls(Collection<File> classpath) throws MalformedURLException {
        List<URL> urls = new ArrayList<>();
        for (File file : classpath) {
            urls.add(file.toURI().toURL());
        }
        return urls.toArray(new URL[0]);
    }

    /**
     * Classes of the directory and of the classpath; JDK classes are found by the bootstrap class loader.
     */
    private static final class FilesHierarchy extends ClassHierarchy {

        private final Map<String, Path> local;

        private final ClassLoader classpath;

        private final Map<String, ClassInfo> cache = new HashMap<>();

        FilesHierarchy(ClassNameFilter targets, Map<String, Path> local, ClassLoader classpath) {
            super(targets);
            this.local = Collections.unmodifiableMap(local);
            this.classpath = classpath;
        }

        @Override
        protected ClassInfo find(String name) {
            if (!cache.containsKey(name)) {
                cache.put(name, read(name));
            }
            return cache.get(name);
        }

        private ClassInfo read(String name) {
            try {
                Path file = local.get(name);
                if (file != null) {
                    return ClassInfo.read(Files.readAllBytes(file));
                }
                URL resource = classpath.getResource(name + CLASS_SUFFIX);
                if (resource == null) {
                    return null;
                }
                try (InputStream in = resource.openStream()) {
                    return ClassInfo.read(ClassFiles.readAll(in));
                }
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        protected boolean isLocal(String name) {
            return local.containsKey(name);
        }

    }

}
