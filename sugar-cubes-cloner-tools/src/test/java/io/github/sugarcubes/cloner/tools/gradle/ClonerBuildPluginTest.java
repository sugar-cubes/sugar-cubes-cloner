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
package io.github.sugarcubes.cloner.tools.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.sugarcubes.cloner.tools.SamplesAccess;

class ClonerBuildPluginTest {

    /**
     * Gradle running the build; the plugin is compiled against the API of Gradle 8 and is tested in both.
     */
    private static final String CURRENT = "current";

    @ParameterizedTest
    @ValueSource(strings = {"8.11.1", CURRENT})
    void pluginEnhancesClasses(String gradleVersion, @TempDir Path project) throws Exception {
        setUp(project, "");

        BuildResult result = build(project, gradleVersion);

        Path classes = project.resolve("build/classes/java/main");
        SamplesAccess.assertEnhanced(classes);
        Assertions.assertTrue(result.getOutput().contains("Copy constructors: 5 enhanced, 0 already enhanced, 2 skipped"),
            result.getOutput());
    }

    @ParameterizedTest
    @ValueSource(strings = {"8.11.1", CURRENT})
    void processorEnhancesClassesBeforePlugin(String gradleVersion, @TempDir Path project) throws Exception {
        String processorPath = Stream.of(System.getProperty("processor.classpath").split(File.pathSeparator))
            .map(path -> "'" + path.replace('\\', '/') + "'")
            .collect(Collectors.joining(", "));
        setUp(project, "dependencies {\n    annotationProcessor files(" + processorPath + ")\n}\n");

        BuildResult result = build(project, gradleVersion);

        SamplesAccess.assertEnhanced(project.resolve("build/classes/java/main"));
        Assertions.assertTrue(result.getOutput().contains("Copy constructors: 0 enhanced, 5 already enhanced, 2 skipped"),
            result.getOutput());
        Assertions.assertTrue(result.getOutput().contains("[sugar-cubes-cloner] no copy constructor"),
            result.getOutput());
    }

    private static void setUp(Path project, String dependencies) throws Exception {
        write(project.resolve("settings.gradle"), "rootProject.name = 'demo'\n");
        write(project.resolve("build.gradle"), "plugins {\n    id 'java'\n    id 'io.github.sugar-cubes.cloner'\n}\n"
            + dependencies);
        Path sources = Files.createDirectories(project.resolve("src/main/java/io/github/sugarcubes/cloner/tools/samples"));
        try (Stream<Path> files = Files.list(SamplesAccess.sources())) {
            for (Path file : (Iterable<Path>) files::iterator) {
                Files.copy(file, sources.resolve(file.getFileName().toString()));
            }
        }
        SamplesAccess.writeMasks(project.resolve("src/main/resources"));
    }

    private static BuildResult build(Path project, String gradleVersion) {
        List<File> pluginClasspath = Stream.of(System.getProperty("processor.classpath").split(File.pathSeparator))
            .map(File::new)
            .collect(Collectors.toList());
        GradleRunner runner = GradleRunner.create()
            .withProjectDir(project.toFile())
            .withPluginClasspath(pluginClasspath)
            .withArguments("compileJava", "--info", "--stacktrace");
        if (!CURRENT.equals(gradleVersion)) {
            runner.withGradleVersion(gradleVersion);
        }
        return runner.build();
    }

    private static void write(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }

}
