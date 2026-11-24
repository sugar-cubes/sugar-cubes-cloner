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
package io.github.sugarcubes.cloner.tools.processor;

import java.io.File;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.sugarcubes.cloner.generation.GenerationTargets;
import io.github.sugarcubes.cloner.tools.ClassInfo;
import io.github.sugarcubes.cloner.tools.SamplesAccess;

class CopyConstructorProcessorTest {

    private static final String TARGETS = "-A" + CopyConstructorProcessor.TARGETS + "="
        + String.join(",", SamplesAccess.masks());

    @Test
    void addsCopyConstructors(@TempDir Path out) throws Exception {
        List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(out, new CopyConstructorProcessor(), TARGETS);

        SamplesAccess.assertEnhanced(out);
        SamplesAccess.assertClonedByCopyConstructors(out);
        assertSkippedReported(diagnostics);
    }

    @Test
    void checksOnly(@TempDir Path out) throws Exception {
        List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(out, new CopyConstructorProcessor(), TARGETS,
            "-A" + CopyConstructorProcessor.ENHANCE + "=false");

        Assertions.assertFalse(ClassInfo.read(SamplesAccess.classFile(out, "Order")).hasCopyConstructor());
        assertSkippedReported(diagnostics);
    }

    @Test
    void masksFile(@TempDir Path out, @TempDir Path resources) throws Exception {
        SamplesAccess.writeMasks(resources);
        Path file = resources.resolve(GenerationTargets.RESOURCE);

        compile(out, new CopyConstructorProcessor(), "-A" + CopyConstructorProcessor.TARGETS_FILE + "=" + file);

        SamplesAccess.assertEnhanced(out);
    }

    @Test
    void gradleWrapper(@TempDir Path out) throws Exception {
        compile(out, new WrappingProcessor(new CopyConstructorProcessor(), GradleLikeEnvironment::new), TARGETS);

        SamplesAccess.assertEnhanced(out);
    }

    @Test
    void intellijWrapper(@TempDir Path out) throws Exception {
        UnaryOperator<ProcessingEnvironment> proxy = environment -> (ProcessingEnvironment) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[] {ProcessingEnvironment.class, WrapperDelegateAccessor.class},
            (instance, method, arguments) -> "getWrapperDelegate".equals(method.getName())
                ? environment : method.invoke(environment, arguments));

        compile(out, new WrappingProcessor(new CopyConstructorProcessor(), proxy), TARGETS);

        SamplesAccess.assertEnhanced(out);
    }

    private static void assertSkippedReported(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        List<String> warnings = diagnostics.stream()
            .filter(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.WARNING && diagnostic.getSource() != null)
            .map(diagnostic -> diagnostic.getSource().getName() + ": " + diagnostic.getMessage(Locale.ROOT))
            .filter(message -> message.contains("[sugar-cubes-cloner]"))
            .collect(Collectors.toList());
        Assertions.assertEquals(2, warnings.size(), warnings.toString());
        Assertions.assertTrue(warnings.stream().anyMatch(w -> w.contains("ExtendsNotListed.java")), warnings.toString());
    }

    private static List<Diagnostic<? extends JavaFileObject>> compile(Path out, Processor processor,
        String... options) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager files = compiler.getStandardFileManager(diagnostics, null, null);
             Stream<Path> sources = Files.list(SamplesAccess.sources())) {
            List<File> list = sources.map(Path::toFile).collect(Collectors.toList());
            List<String> arguments = new ArrayList<>(Arrays.asList("-d", out.toString()));
            arguments.addAll(Arrays.asList(options));
            JavaCompiler.CompilationTask task = compiler.getTask(null, files, diagnostics, arguments, null,
                files.getJavaFileObjectsFromFiles(list));
            task.setProcessors(Arrays.asList(processor));
            Assertions.assertTrue(task.call(), diagnostics.getDiagnostics().toString());
        }
        return diagnostics.getDiagnostics();
    }

    /**
     * Passes a wrapped environment to the processor, as build tools do.
     */
    private static final class WrappingProcessor extends AbstractProcessor {

        private final Processor delegate;

        private final UnaryOperator<ProcessingEnvironment> wrapper;

        WrappingProcessor(Processor delegate, UnaryOperator<ProcessingEnvironment> wrapper) {
            this.delegate = delegate;
            this.wrapper = wrapper;
        }

        @Override
        public synchronized void init(ProcessingEnvironment environment) {
            super.init(environment);
            delegate.init(wrapper.apply(environment));
        }

        @Override
        public Set<String> getSupportedAnnotationTypes() {
            return delegate.getSupportedAnnotationTypes();
        }

        @Override
        public Set<String> getSupportedOptions() {
            return delegate.getSupportedOptions();
        }

        @Override
        public SourceVersion getSupportedSourceVersion() {
            return delegate.getSupportedSourceVersion();
        }

        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
            return delegate.process(annotations, round);
        }

    }

}
