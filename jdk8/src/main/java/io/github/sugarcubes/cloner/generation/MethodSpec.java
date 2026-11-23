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
package io.github.sugarcubes.cloner.generation;

import java.util.function.Consumer;

/**
 * Method of a generated class.
 *
 * @author Maxim Butov
 */
public final class MethodSpec {

    private final int access;

    private final String name;

    private final String descriptor;

    private final Consumer<CodeEmitter> body;

    /**
     * Creates the method.
     *
     * @param access access flags ({@link java.lang.reflect.Modifier} values)
     * @param name method name
     * @param descriptor method descriptor
     * @param body emits the instructions
     */
    public MethodSpec(int access, String name, String descriptor, Consumer<CodeEmitter> body) {
        this.access = access;
        this.name = name;
        this.descriptor = descriptor;
        this.body = body;
    }

    /**
     * Returns the access flags.
     *
     * @return access flags
     */
    public int getAccess() {
        return access;
    }

    /**
     * Returns the method name.
     *
     * @return method name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the method descriptor.
     *
     * @return method descriptor
     */
    public String getDescriptor() {
        return descriptor;
    }

    /**
     * Emits the instructions of the method.
     *
     * @param code emitter of the backend
     */
    public void emit(CodeEmitter code) {
        body.accept(code);
    }

}
