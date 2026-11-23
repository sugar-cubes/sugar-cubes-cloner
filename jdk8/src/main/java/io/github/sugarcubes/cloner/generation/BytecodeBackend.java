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

import java.util.List;

/**
 * Writer of class files: ASM or the ClassFile API of JDK 24+. The copier template does not depend on it.
 *
 * @author Maxim Butov
 */
public interface BytecodeBackend {

    /**
     * Writes a final synthetic class.
     *
     * @param version class file version
     * @param name internal name of the class
     * @param superName internal name of the super class
     * @param methods methods
     * @return class file
     */
    byte[] write(int version, String name, String superName, List<MethodSpec> methods);

}
