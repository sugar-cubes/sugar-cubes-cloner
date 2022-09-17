/*
 * Copyright 2017-2022 the original author or authors.
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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Object allocator using {@link MethodHandle} to invoke constructor.
 *
 * @author Maxim Butov
 */
public class MethodHandleObjectAllocator implements ObjectAllocator {

    /**
     * Trusted {@link MethodHandles.Lookup} instance.
     */
    private static final MethodHandles.Lookup LOOKUP = MethodHandlesLookupUtils.getLookup();

    /**
     * Constructor's method type.
     */
    private static final MethodType VOID_METHOD_TYPE = MethodType.methodType(void.class);

    @Override
    public <T> ObjectFactory<T> getFactory(Class<T> type) {
        MethodHandle constructor = ReflectionUtils.execute(() -> LOOKUP.findConstructor(type, VOID_METHOD_TYPE));
        return () -> newInstance(constructor);
    }

    private static <T> T newInstance(MethodHandle constructor) {
        try {
            return (T) constructor.invoke();
        }
        catch (Throwable e) {
            throw new ClonerException(e);
        }
    }

}
