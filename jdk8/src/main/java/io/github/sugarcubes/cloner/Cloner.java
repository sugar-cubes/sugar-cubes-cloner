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
package io.github.sugarcubes.cloner;

/**
 * Cloner interface.
 *
 * @author Maxim Butov
 */
public interface Cloner {

    /**
     * Creates a deep clone of the object.
     *
     * @param <T> object type
     * @param object object to clone
     * @return a clone
     * @throws ClonerException if something went wrong
     */
    <T> T clone(T object) throws ClonerException;

}
