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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import static io.github.sugarcubes.cloner.ClonerExceptionUtils.replaceException;

/**
 * Utility class to get trusted lookup instance.
 *
 * @author Maxim Butov
 */
public class MethodHandlesLookupUtils {

    /**
     * Trusted {@link MethodHandles.Lookup} instance.
     */
    private static final MethodHandles.Lookup LOOKUP;

    static {
        Field implLookup = ReflectionUtils.getField(MethodHandles.Lookup.class, "IMPL_LOOKUP");
        LOOKUP = (MethodHandles.Lookup) replaceException(() -> implLookup.get(null));
    }

    /**
     * Returns trusted {@link MethodHandles.Lookup} instance.
     * @return trusted {@link MethodHandles.Lookup} instance
     */
    public static MethodHandles.Lookup getLookup() {
        return LOOKUP;
    }

    /**
     * Utility class.
     */
    private MethodHandlesLookupUtils() {
    }

}
