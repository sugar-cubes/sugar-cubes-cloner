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

import io.github.sugarcubes.cloner.ClonerException;

/**
 * Creates {@link UnsafeBridge} for JDK 9+: prefers {@link jdk.internal.misc.Unsafe} and falls back to
 * {@link sun.misc.Unsafe} if {@code jdk.internal.misc} is not exported to the cloner.
 *
 * @author Maxim Butov
 */
class UnsafeBridgeProvider$Jdk9 extends UnsafeBridgeProvider$Jdk8 {

    @Override
    UnsafeBridge newInstance(Reflection reflection) {
        try {
            return new JdkInternalMiscUnsafeBridge();
        }
        catch (LinkageError | ClonerException e) {
            // IllegalAccessError when jdk.internal.misc is not exported, fallback to the classics
            return super.newInstance(reflection);
        }
    }

}
