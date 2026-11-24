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
package io.github.sugarcubes.cloner.platform;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;

import io.github.sugarcubes.cloner.generation.BytecodeBackend;

/**
 * Definition of classes with the private access of a host class: anonymous classes of {@code sun.misc.Unsafe} on JDK
 * 8-14, hidden classes in the nest of the host on JDK 15+.
 *
 * @author Maxim Butov
 */
public interface ClassDefiner {

    /**
     * Tells whether classes can be defined.
     *
     * @return {@code true} if {@link #defineNestmate} works
     */
    boolean isAvailable();

    /**
     * Returns the version of the class files {@link #defineNestmate} takes.
     *
     * @return class file version
     */
    int classVersion();

    /**
     * Tells whether a defined class gets class data, which its initializer reads with
     * {@code MethodHandles.classDataAt}, JDK 16+.
     *
     * @return {@code true} if class data is supported
     */
    boolean supportsClassData();

    /**
     * Defines a class with the private access of the host class; the package of the support class (the super class
     * of the defined one) is exported to the module of the host if needed.
     *
     * @param host host class
     * @param bytes class file of a class in the package of the host
     * @param support class of the cloner the defined class uses
     * @param constructor type of the constructor of the defined class
     * @param classData class data of the defined class, {@code null} if none; needs {@link #supportsClassData()}
     * @return the constructor
     * @throws io.github.sugarcubes.cloner.ClonerException if the class cannot be defined
     */
    MethodHandle defineNestmate(Class<?> host, byte[] bytes, Class<?> support, MethodType constructor,
        List<?> classData);

    /**
     * Returns the backend of the ClassFile API, JDK 24+.
     *
     * @return backend or empty on older JDKs
     */
    Optional<BytecodeBackend> classFileBackend();

}
