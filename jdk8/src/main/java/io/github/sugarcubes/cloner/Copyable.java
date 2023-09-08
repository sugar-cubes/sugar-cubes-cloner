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
 * An interface for classes which know how to clone themselves. Similar to {@link Cloneable}.
 *
 * @author Maxim Butov
 */
public interface Copyable<T> {

    /**
     * Creates deep copy of this object.
     * The implementation MUST call {@link CopyContext#register(Object)} method.
     *
     * @param context copying context
     * @return clone of this object
     */
    T copy(CopyContext context);

}
