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

import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * Utility class to obtain {@link Unsafe} instance.
 *
 * @author Maxim Butov
 */
public class UnsafeUtils {

    /**
     * The {@link Unsafe#theUnsafe}.
     */
    private static final Unsafe UNSAFE;

    static {
        UNSAFE = ClonerExceptionUtils.replaceException(() -> {
                Field theUnsafe = ReflectionUtils.getField(Unsafe.class, "theUnsafe");
                return (Unsafe) theUnsafe.get(null);
            }
        );
    }

    /**
     * Returns {@link Unsafe} instance.
     *
     * @return {@link Unsafe} instance
     */
    public static Unsafe getUnsafe() {
        return UNSAFE;
    }

    /**
     * Utility class.
     */
    private UnsafeUtils() {
    }

}
