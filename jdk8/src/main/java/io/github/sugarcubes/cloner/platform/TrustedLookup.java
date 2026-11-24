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
package io.github.sugarcubes.cloner.platform;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import io.github.sugarcubes.cloner.unsafe.UnsafeBridge;
import static io.github.sugarcubes.cloner.util.ClonerExceptionUtils.replaceException;

/**
 * The trusted lookup {@code MethodHandles.Lookup.IMPL_LOOKUP}, which has access to any member of any class.
 */
final class TrustedLookup {

    private static final String FIELD = "IMPL_LOOKUP";

    /**
     * The lookup read by reflection when first needed.
     */
    private static final class ByReflection {

        static final MethodHandles.Lookup LOOKUP = replaceException(() -> {
            Field field = MethodHandles.Lookup.class.getDeclaredField(FIELD);
            field.setAccessible(true);
            return (MethodHandles.Lookup) field.get(null);
        });

    }

    /**
     * Reads the lookup by reflection, which needs {@code java.base/java.lang.invoke} opened to the cloner on JDK 9+.
     *
     * @return trusted lookup
     * @throws ExceptionInInitializerError on the first failure, {@link NoClassDefFoundError} on the next ones
     */
    static MethodHandles.Lookup byReflection() {
        return ByReflection.LOOKUP;
    }

    /**
     * Reads the lookup by {@code Unsafe}, which needs no opened packages.
     *
     * @param unsafe Unsafe bridge
     * @return trusted lookup
     */
    static MethodHandles.Lookup byUnsafe(UnsafeBridge unsafe) {
        return replaceException(() -> {
            Field field = MethodHandles.Lookup.class.getDeclaredField(FIELD);
            return (MethodHandles.Lookup) unsafe.getObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field));
        });
    }

    private TrustedLookup() {
    }

}
