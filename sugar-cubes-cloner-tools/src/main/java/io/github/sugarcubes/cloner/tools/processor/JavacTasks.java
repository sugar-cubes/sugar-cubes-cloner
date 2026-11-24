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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;

import com.sun.source.util.JavacTask;

/**
 * Finds the javac task of a processing environment. Build tools wrap the environment of javac: Gradle for incremental
 * annotation processing ({@code IncrementalProcessingEnvironment}, the delegate in a field), IntelliJ IDEA in a proxy
 * ({@code APIWrappers.WrapperDelegateAccessor.getWrapperDelegate()}); {@link JavacTask#instance(ProcessingEnvironment)}
 * accepts the environment of javac only.
 *
 * @author Maxim Butov
 */
final class JavacTasks {

    private static final int MAX_WRAPPERS = 5;

    private JavacTasks() {
    }

    /**
     * Finds the javac task of the environment.
     *
     * @param environment processing environment, possibly wrapped
     * @return javac task or empty if the environment is not of javac (e.g. Eclipse) or cannot be unwrapped
     */
    static Optional<JavacTask> of(ProcessingEnvironment environment) {
        Object current = environment;
        for (int depth = 0; depth <= MAX_WRAPPERS && current instanceof ProcessingEnvironment; depth++) {
            try {
                return Optional.of(JavacTask.instance((ProcessingEnvironment) current));
            }
            catch (IllegalArgumentException | ClassCastException e) {
                current = unwrap(current);
            }
        }
        return Optional.empty();
    }

    private static Object unwrap(Object wrapper) {
        try {
            // IntelliJ IDEA
            return wrapper.getClass().getMethod("getWrapperDelegate").invoke(wrapper);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | RuntimeException e) {
            // not IntelliJ IDEA
        }
        for (Class<?> type = wrapper.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (ProcessingEnvironment.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        return field.get(wrapper);
                    }
                    catch (IllegalAccessException | RuntimeException e) {
                        // try the next field
                    }
                }
            }
        }
        return null;
    }

}
