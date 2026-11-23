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
package io.github.sugarcubes.cloner.platform;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.function.Supplier;

import io.github.sugarcubes.cloner.ModuleOpening;
import io.github.sugarcubes.cloner.spi.FieldAdapterFactory;
import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.unsafe.UnsafeBridge;
import io.github.sugarcubes.cloner.util.Checks;

/**
 * Low-level access: reflection which opens modules, {@code Unsafe}, {@code VarHandle}, the trusted lookup.
 *
 * @author Maxim Butov
 */
public interface LowLevelAccess {

    /**
     * Creates reflection which opens packages to the cloner with the mechanism before making members accessible (JDK
     * 9+), fails with {@link io.github.sugarcubes.cloner.ClonerException} if the mechanism is not available.
     *
     * @param opening module opening mechanism
     * @param unsafe Unsafe bridge, requested only if the mechanism needs it
     * @return reflection
     */
    Reflection reflection(ModuleOpening opening, Supplier<UnsafeBridge> unsafe);

    /**
     * Creates reflection which opens nothing.
     *
     * @return reflection
     */
    default Reflection reflection() {
        return reflection(ModuleOpening.NONE, () -> {
            throw Checks.mustNotHappen();
        });
    }

    /**
     * Creates the bridge to {@code Unsafe}: {@code jdk.internal.misc.Unsafe} on JDK 9+ if it is exported to the
     * cloner, {@code sun.misc.Unsafe} otherwise.
     *
     * @param reflection reflection to read {@code sun.misc.Unsafe}
     * @return Unsafe bridge
     */
    UnsafeBridge unsafeBridge(Reflection reflection);

    /**
     * Returns the factory of field adapters using {@code VarHandle}, JDK 9+.
     *
     * @return factory or empty on JDK 8
     */
    Optional<FieldAdapterFactory> varHandleFieldAdapters();

    /**
     * Returns the trusted lookup {@code MethodHandles.Lookup.IMPL_LOOKUP}, read by reflection: on JDK 9+ it needs
     * {@code java.base/java.lang.invoke} opened to the cloner.
     *
     * @return trusted lookup
     * @throws LinkageError if the lookup cannot be read
     */
    MethodHandles.Lookup trustedLookup();

}
