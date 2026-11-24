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

/**
 * Everything which differs between JDK versions, in four parts: {@link TypeKinds}, {@link TypeModels},
 * {@link LowLevelAccess} and {@link ClassDefiner}. Every part has a chain of versions ({@code TypeKinds8},
 * {@code TypeKinds9}, ...), the class {@code JdkPlatformImpl} of the running JDK, chosen by the multi-release jar,
 * combines the parts of its version. The directory of a version contains only the parts which change in it.
 *
 * @author Maxim Butov
 */
public interface JdkPlatform {

    /**
     * Returns the platform of the running JDK.
     *
     * @return platform
     */
    static JdkPlatform current() {
        return Current.PLATFORM;
    }

    /**
     * Returns the kinds of types: immutable, cloneable, objects of the running JVM, hidden classes.
     *
     * @return type kinds
     */
    TypeKinds typeKinds();

    /**
     * Returns the low-level models of types: containers, immutable collections, records.
     *
     * @return type models
     */
    TypeModels typeModels();

    /**
     * Returns the low-level access: reflection opening modules, {@code Unsafe}, {@code VarHandle}, the trusted lookup.
     *
     * @return low-level access
     */
    LowLevelAccess lowLevelAccess();

    /**
     * Returns the definition of classes with the private access of a host class.
     *
     * @return class definer
     */
    ClassDefiner classDefiner();

    /**
     * Holder of the current platform, not for direct use.
     */
    final class Current {

        static final JdkPlatform PLATFORM = new JdkPlatformImpl();

        private Current() {
        }

    }

}
