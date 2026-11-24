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
package io.github.sugarcubes.cloner.tools.maven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import io.github.sugarcubes.cloner.generation.GenerationTargets;
import io.github.sugarcubes.cloner.tools.ClassesDirectoryEnhancer;

/**
 * Goals {@code enhance} and {@code enhance-test}: add copy constructors to the compiled classes listed by masks in
 * {@value GenerationTargets#RESOURCE} of the resources. Maven copies the resources to the output directory before
 * compilation, the goal reads them there. The parameters are described in {@code META-INF/maven/plugin.xml}.
 *
 * @author Maxim Butov
 */
public class EnhanceMojo extends AbstractMojo {

    /**
     * Directory of the compiled classes.
     */
    private File classesDirectory;

    /**
     * Classpath of the compilation, to read superclasses.
     */
    private List<String> classpathElements;

    /**
     * Skips the goal.
     */
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Copy constructors are skipped.");
            return;
        }
        List<String> masks = ClassesDirectoryEnhancer.readMasks(Collections.singletonList(classesDirectory));
        if (masks.isEmpty()) {
            getLog().debug(String.format("No %s, no copy constructors.", GenerationTargets.RESOURCE));
            return;
        }
        List<File> classpath = new ArrayList<>();
        if (classpathElements != null) {
            classpathElements.forEach(element -> classpath.add(new File(element)));
        }
        try {
            ClassesDirectoryEnhancer.Report report = ClassesDirectoryEnhancer.enhance(classesDirectory.toPath(),
                classpath, masks, getLog()::warn);
            getLog().info("Copy constructors: " + report);
        }
        catch (IOException e) {
            throw new MojoExecutionException("Cannot add copy constructors to " + classesDirectory, e);
        }
    }

}
