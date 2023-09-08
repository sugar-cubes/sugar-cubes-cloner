/*
 * Copyright 2017-2023 the original author or authors.
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
package io.github.sugarcubes.cloner;

/**
 * Factory for creating different {@link Cloner}s.
 *
 * @author Maxim Butov
 */
public class Cloners {

    /**
     * Returns a new instance of {@link ReflectionClonerBuilder}.
     *
     * @return reflection cloner builder
     */
    public static ReflectionClonerBuilder builder() {
        return new ReflectionClonerBuilder();
    }

    /**
     * Returns an instance of {@link Cloner} with default settings.
     *
     * @return reflection sequential cloner
     */
    public static Cloner reflection() {
        return builder().build();
    }

    /**
     * Returns an instance of {@link SerializationCloner}.
     *
     * @return {@link SerializationCloner} instance
     */
    public static Cloner serialization() {
        return SerializationCloner.INSTANCE;
    }

    /**
     * Clones object using {@link #reflection()} cloner.
     *
     * @param <T> object type
     * @param object original object
     * @return clone
     */
    public static <T> T reflectionClone(T object) {
        return reflection().clone(object);
    }

    /**
     * Clones object using {@link #serialization()} cloner.
     *
     * @param <T> object type
     * @param object original object
     * @return clone
     */
    public static <T> T serializationClone(T object) {
        return serialization().clone(object);
    }

    /**
     * Utility class.
     */
    private Cloners() {
    }

}
