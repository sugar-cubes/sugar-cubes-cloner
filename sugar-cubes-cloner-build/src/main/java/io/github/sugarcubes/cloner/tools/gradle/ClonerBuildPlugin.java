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
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.process.CommandLineArgumentProvider;

import io.github.sugarcubes.cloner.generation.GenerationTargets;
import io.github.sugarcubes.cloner.tools.ClassesDirectoryEnhancer;
import io.github.sugarcubes.cloner.tools.processor.CopyConstructorProcessor;

/**
 * Gradle plugin {@code io.github.sugar-cubes.cloner}: after the compilation of every source set adds copy constructors
 * to the classes listed by masks in {@value GenerationTargets#RESOURCE} of the resources of the source set. If the
 * annotation processor of the cloner is on the processor path, the plugin passes it the masks files too, and the
 * plugin finds the classes already enhanced.
 *
 * @author Maxim Butov
 */
public class ClonerBuildPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().withType(JavaBasePlugin.class, java -> {
            SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
            sourceSets.all(sourceSet -> project.getTasks()
                .named(sourceSet.getCompileJavaTaskName(), JavaCompile.class)
                .configure(task -> configure(project, task, sourceSet)));
        });
    }

    private static void configure(Project project, JavaCompile task, SourceSet sourceSet) {
        FileCollection masks = project.files(project.provider(() -> sourceSet.getResources().getSrcDirs().stream()
            .map(dir -> new File(dir, GenerationTargets.RESOURCE))
            .collect(Collectors.toList())))
            .filter(File::isFile);
        task.getInputs().files(masks).withPropertyName("copyConstructorTargets")
            .withPathSensitivity(PathSensitivity.RELATIVE).optional();
        task.getOptions().getCompilerArgumentProviders().add(new TargetsArgument(masks));
        task.doLast(new EnhanceAction(masks, task.getClasspath()));
    }

    /**
     * Passes the masks files to the annotation processor, if it is on the processor path.
     */
    private static final class TargetsArgument implements CommandLineArgumentProvider {

        private final FileCollection masks;

        TargetsArgument(FileCollection masks) {
            this.masks = masks;
        }

        @Override
        public Iterable<String> asArguments() {
            if (masks.isEmpty()) {
                return Collections.emptyList();
            }
            return Collections.singletonList(String.format("-A%s=%s", CopyConstructorProcessor.TARGETS_FILE,
                masks.getAsPath()));
        }

    }

    /**
     * Adds copy constructors to the classes of the compilation.
     */
    private static final class EnhanceAction implements Action<Task> {

        private final FileCollection masks;

        private final FileCollection classpath;

        EnhanceAction(FileCollection masks, FileCollection classpath) {
            this.masks = masks;
            this.classpath = classpath;
        }

        @Override
        public void execute(Task task) {
            List<String> lines = new ArrayList<>();
            for (File file : masks.getFiles()) {
                try {
                    lines.addAll(GenerationTargets.parse(file.toURI().toURL()));
                }
                catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            if (lines.isEmpty()) {
                return;
            }
            File classes = ((JavaCompile) task).getDestinationDirectory().get().getAsFile();
            try {
                ClassesDirectoryEnhancer.Report report = ClassesDirectoryEnhancer.enhance(classes.toPath(),
                    classpath.getFiles(), lines, task.getLogger()::warn);
                task.getLogger().info("Copy constructors: {}", report);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

    }

}
