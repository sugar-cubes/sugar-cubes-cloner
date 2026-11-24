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
package io.github.sugarcubes.cloner.engine;

import io.github.sugarcubes.cloner.platform.JdkPlatform;
import io.github.sugarcubes.cloner.util.ClonerExceptionUtils;

/**
 * Tells whether hash codes of copied keys are known at once, when the keys are put into a container being copied.
 * A hash code computed from the state of a copied object may be final only when the whole object graph is copied.
 *
 * @author Maxim Butov
 */
final class HashKeys {

    /**
     * Whether a class has its own hash code instead of the identity one.
     */
    private static final ClassValue<Boolean> HASHED_BY_STATE = new ClassValue<Boolean>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            return ClonerExceptionUtils.replaceException(() -> type.getMethod("hashCode").getDeclaringClass())
                != Object.class;
        }
    };

    /**
     * Checks the keys among elements.
     *
     * @param elements elements of a set, or keys and values of a map in pairs
     * @param map whether the elements are keys and values in pairs
     * @return {@code true} if hash codes of all keys are known at once
     */
    static boolean knownAtOnce(Object[] elements, boolean map) {
        int step = map ? 2 : 1;
        for (int k = 0; k < elements.length; k += step) {
            if (!knownAtOnce(elements[k])) {
                return false;
            }
        }
        return true;
    }

    /**
     * A hash code is known at once if the key is not copied (e.g. a string or an enum) or is hashed by identity.
     */
    private static boolean knownAtOnce(Object key) {
        if (key == null || key instanceof Enum) {
            return true;
        }
        Class<?> type = key.getClass();
        return JdkPlatform.current().typeKinds().isImmutable(type) || !HASHED_BY_STATE.get(type);
    }

    private HashKeys() {
    }

}
