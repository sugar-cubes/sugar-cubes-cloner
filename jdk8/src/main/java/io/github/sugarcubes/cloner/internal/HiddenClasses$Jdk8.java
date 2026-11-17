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
package io.github.sugarcubes.cloner.internal;

/**
 * Recognizes classes whose instances have no accessible structure on JDK 8..14: lambda classes, which are synthetic
 * VM anonymous classes with {@code $$Lambda} in the name.
 *
 * @author Maxim Butov
 */
class HiddenClasses$Jdk8 {

    /**
     * Tells whether the class is hidden: it cannot be found by name, its instances cannot be allocated by name and its
     * final fields are not writable.
     *
     * @param type class
     * @return {@code true} if the class is hidden
     */
    public boolean isHidden(Class<?> type) {
        return type.isSynthetic() && type.getName().contains("$$Lambda");
    }

}
