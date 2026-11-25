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
package io.github.sugarcubes.cloner;

/**
 * Actions which can be applied to objects, types and fields: what the clone gets instead of the original value.
 *
 * @author Maxim Butov
 */
public enum CopyAction {

    /**
     * Clone the object, unless it is immutable or shared by default. For policies: the policy does not decide.
     */
    DEFAULT,

    /**
     * Use the original object.
     */
    ORIGINAL,

    /**
     * The clone gets {@code null}; a primitive field gets its default value, {@code 0} or {@code false}. Field
     * initializers do not run, the clones are created without constructors.
     */
    CLEAR,

}
