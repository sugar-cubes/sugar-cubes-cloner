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
import java.util.Optional;
import java.util.function.Supplier;

import io.github.sugarcubes.cloner.ModuleOpening;
import io.github.sugarcubes.cloner.reflection.ReflectionImpl8;
import io.github.sugarcubes.cloner.spi.FieldAdapterFactory;
import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.unsafe.SunMiscUnsafeBridge;
import io.github.sugarcubes.cloner.unsafe.UnsafeBridge;
import io.github.sugarcubes.cloner.util.Lazy;

/**
 * {@link LowLevelAccess} of JDK 8: no modules, {@code sun.misc.Unsafe}.
 *
 * @author Maxim Butov
 */
class LowLevelAccess8 implements LowLevelAccess {

    @Override
    public Reflection reflection(ModuleOpening opening, Supplier<UnsafeBridge> unsafe) {
        // no modules, nothing to open
        return new ReflectionImpl8();
    }

    /**
     * The bridge, created when first needed.
     */
    private final Lazy<UnsafeBridge> unsafeBridge = new Lazy<>(this::newUnsafeBridge);

    @Override
    public UnsafeBridge unsafeBridge() {
        return unsafeBridge.get();
    }

    /**
     * Creates the bridge to {@code Unsafe} of this JDK version.
     *
     * @return Unsafe bridge
     */
    UnsafeBridge newUnsafeBridge() {
        return new SunMiscUnsafeBridge();
    }

    @Override
    public Optional<FieldAdapterFactory> varHandleFieldAdapters() {
        return Optional.empty();
    }

    @Override
    public MethodHandles.Lookup trustedLookup() {
        return TrustedLookup.byReflection();
    }

}
