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
package org.sugarcubes.cloner;

import java.util.Collection;
import java.util.Set;

/**
 * Interface for obtaining specific classes/objects from JDK implementation.
 *
 * @author Maxim Butov
 */
interface JdkConfiguration {

    /**
     * Returns immutable types.
     *
     * @return set of immutable types
     */
    Set<Class<?>> getImmutableTypes();

    /**
     * Returns cloneable types.
     *
     * @return set of cloneable types
     */
    Set<Class<?>> getCloneableTypes();

    /**
     * Returns system-wide JDK singletons.
     *
     * @return system-wide JDK singletons
     */
    Collection<Object> getSystemWideSingletons();

}
