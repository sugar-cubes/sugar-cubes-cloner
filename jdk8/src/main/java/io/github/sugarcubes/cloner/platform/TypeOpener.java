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
package io.github.sugarcubes.cloner.platform;

/**
 * Opens the package of a type to the cloner for deep reflection, a part of {@link ReflectionImpl$Jdk9 reflection} on
 * JDK 9+.
 */
interface TypeOpener {

    /**
     * Opener which opens nothing.
     */
    TypeOpener NOOP = new TypeOpener() {
        @Override
        public void open(Class<?> type) {
        }
    };

    /**
     * Opens the package of the type to the cloner if the mechanism can do it.
     *
     * @param type type
     */
    void open(Class<?> type);

}
