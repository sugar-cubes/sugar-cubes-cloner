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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.spi.TypeOpener;

/**
 * {@link JdkPlatform} of JDK 9+: modules, {@code jdk.internal.misc.Unsafe}, immutable collections.
 *
 * @author Maxim Butov
 */
class JdkPlatformImpl$Jdk9 extends JdkPlatformImpl$Jdk8 {

    JdkPlatformImpl$Jdk9() {
        systemWideSingletons.addAll(Arrays.asList(List.of(), Set.of(), Map.of()));
    }

    @Override
    public Reflection reflection(TypeOpener opener) {
        return new ReflectionImpl$Jdk9(opener);
    }

    @Override
    public UnsafeBridge unsafeBridge(Reflection reflection) {
        try {
            return newJdkInternalMiscUnsafeBridge();
        }
        catch (LinkageError | ClonerException e) {
            // IllegalAccessError when jdk.internal.misc is not exported, fallback to sun.misc.Unsafe
            return super.unsafeBridge(reflection);
        }
    }

    /**
     * Creates the bridge to {@code jdk.internal.misc.Unsafe} of this JDK version.
     *
     * @return Unsafe bridge
     */
    UnsafeBridge newJdkInternalMiscUnsafeBridge() {
        return new JdkInternalMiscUnsafeBridge$Jdk9();
    }

}
