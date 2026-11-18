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
package io.github.sugarcubes.cloner.util;

/**
 * Narcissus library utilities.
 *
 * @author Maxim Butov
 */
public final class NarcissusUtils {

    private static final String NARCISSUS_CLASS = "io.github.toolfactory.narcissus.Narcissus";

    /**
     * Tells whether the Narcissus library is on the classpath and its native part is loaded.
     *
     * @return {@code true} if Narcissus can be used
     */
    public static boolean isNarcissusAvailable() {
        if (!ClassUtils.isClassAvailable(NARCISSUS_CLASS)) {
            return false;
        }
        return ClonerExceptionUtils.replaceException(
            () -> Class.forName(NARCISSUS_CLASS).getField("libraryLoaded").getBoolean(null));
    }

    private NarcissusUtils() {
    }

}
