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
package io.github.sugarcubes.cloner.tools.processor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;

import io.github.sugarcubes.cloner.generation.ClassNameFilter;
import io.github.sugarcubes.cloner.generation.GenerationTargets;
import io.github.sugarcubes.cloner.tools.ClassFiles;
import io.github.sugarcubes.cloner.tools.CopyConstructorEnhancer;

/**
 * Annotation processor which adds copy constructors (see {@link io.github.sugarcubes.cloner.generation.CopyConstructors})
 * to the compiled classes listed by masks, without annotations: it processes all types of the compilation and
 * rewrites a class file right after javac has written it.
 *
 * <p>The masks are taken, in this order, from the options {@value #TARGETS} (masks separated by commas or spaces) and
 * {@value #TARGETS_FILE} (paths of masks files), or from the resource {@value GenerationTargets#RESOURCE} in the class
 * output (Maven copies resources before compilation). Option {@value #ENHANCE}{@code =false} only checks the classes:
 * a build plugin adds the constructors then.</p>
 *
 * <p>Class files are rewritten by a listener of the javac task; Gradle and IntelliJ IDEA wrap the processing
 * environment, the processor unwraps it. Other compilers (Eclipse) leave the classes as they are, the cloner copies
 * them as usual.</p>
 *
 * @author Maxim Butov
 */
public class CopyConstructorProcessor extends AbstractProcessor {

    /**
     * Option with masks separated by commas or spaces.
     */
    public static final String TARGETS = "sugarcubes.cloner.targets";

    /**
     * Option with the paths of masks files, separated by the path separator.
     */
    public static final String TARGETS_FILE = "sugarcubes.cloner.targetsFile";

    /**
     * Option which disables rewriting of class files: {@code false} only checks the classes.
     */
    public static final String ENHANCE = "sugarcubes.cloner.enhance";

    private static final String PREFIX = "[sugar-cubes-cloner] ";

    private ElementsHierarchy hierarchy;

    private boolean enhance;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("*");
    }

    @Override
    public Set<String> getSupportedOptions() {
        return new HashSet<>(Arrays.asList(TARGETS, TARGETS_FILE, ENHANCE));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment environment) {
        super.init(environment);
        List<String> masks = masks();
        if (masks.isEmpty()) {
            return;
        }
        hierarchy = new ElementsHierarchy(ClassNameFilter.of(masks), environment.getElementUtils(),
            environment.getFiler());
        enhance = !"false".equals(environment.getOptions().get(ENHANCE));
        if (!enhance) {
            return;
        }
        Optional<JavacTask> task = JavacTasks.of(environment);
        if (task.isPresent()) {
            task.get().addTaskListener(new TaskListener() {

                @Override
                public void started(TaskEvent event) {
                }

                @Override
                public void finished(TaskEvent event) {
                    if (event.getKind() == TaskEvent.Kind.GENERATE && event.getTypeElement() != null) {
                        generated(event.getTypeElement());
                    }
                }

            });
        }
        else {
            warning(null, String.format("cannot add copy constructors: the compiler is not javac or its environment "
                + "%s is not supported; use the Maven or Gradle plugin", environment.getClass().getName()));
            enhance = false;
        }
    }

    private List<String> masks() {
        List<String> masks = new ArrayList<>();
        String targets = processingEnv.getOptions().get(TARGETS);
        if (targets != null) {
            for (String mask : targets.split("[,\\s]+")) {
                if (!mask.isEmpty()) {
                    masks.add(mask);
                }
            }
        }
        try {
            String file = processingEnv.getOptions().get(TARGETS_FILE);
            if (file != null) {
                for (String path : file.split(File.pathSeparator)) {
                    if (!path.isEmpty()) {
                        masks.addAll(GenerationTargets.parse(new File(path).toURI().toURL()));
                    }
                }
            }
            else if (targets == null) {
                FileObject resource = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "",
                    GenerationTargets.RESOURCE);
                masks.addAll(GenerationTargets.parse(resource.toUri().toURL()));
            }
        }
        catch (IOException | IllegalArgumentException e) {
            // no masks file
        }
        return masks;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
        if (hierarchy == null) {
            return false;
        }
        for (Element element : round.getRootElements()) {
            if (element instanceof TypeElement) {
                hierarchy.addLocal((TypeElement) element);
            }
        }
        if (round.processingOver()) {
            // reported now, at the sources: when javac writes class files, the sources are not available any more
            for (String name : hierarchy.localNames()) {
                Optional<String> problem = hierarchy.targetProblem(name);
                if (problem.isPresent()) {
                    warning(hierarchy.getLocal(name), "no copy constructor: " + problem.get());
                }
            }
        }
        return false;
    }

    /**
     * Rewrites the class file of the type which javac has just written.
     *
     * @param type type
     */
    private void generated(TypeElement type) {
        String name = hierarchy.internalName(type);
        if (!hierarchy.isTarget(name) || hierarchy.getLocal(name) == null) {
            return;
        }
        try {
            int slash = name.lastIndexOf('/');
            String packageName = slash >= 0 ? name.substring(0, slash).replace('/', '.') : "";
            FileObject file = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, packageName,
                name.substring(slash + 1) + ".class");
            URI uri = file.toUri();
            if (!"file".equals(uri.getScheme())) {
                warning(type, "cannot add the copy constructor: the class file is not in a directory");
                return;
            }
            Path path = Paths.get(uri);
            byte[] bytes;
            try (InputStream in = Files.newInputStream(path)) {
                bytes = ClassFiles.readAll(in);
            }
            CopyConstructorEnhancer.Result result = CopyConstructorEnhancer.enhance(bytes, hierarchy);
            if (result.getStatus() == CopyConstructorEnhancer.Status.ENHANCED) {
                Files.write(path, result.getBytes());
            }
        }
        catch (IOException | RuntimeException e) {
            warning(type, "cannot add the copy constructor: " + e);
        }
    }

    private void warning(Element element, String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, PREFIX + message, element);
    }

}
