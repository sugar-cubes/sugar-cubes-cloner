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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.sugarcubes.cloner.spi.TypeOpener;
import io.github.sugarcubes.cloner.unsafe.SunMiscUnsafeBridge;
import io.github.sugarcubes.cloner.unsafe.UnsafeBridge;

class UnsafeBridgeTest {

    @Test
    void prefersJdkInternalMiscUnsafeWhenExported() throws Exception {
        // tests run with --add-opens java.base/jdk.internal.misc=ALL-UNNAMED
        UnsafeBridge unsafe = JdkPlatform.current().unsafeBridge(JdkPlatform.current().reflection(TypeOpener.NOOP));

        Assertions.assertInstanceOf(JdkInternalMiscUnsafeBridge$Jdk9.class, unsafe);
        Assertions.assertInstanceOf(Object.class, unsafe.allocateInstance(Object.class));
    }

    @Test
    void sunMiscUnsafeBridgeWorks() throws Exception {
        UnsafeBridge unsafe = new SunMiscUnsafeBridge(JdkPlatform.current().reflection(TypeOpener.NOOP));

        Assertions.assertInstanceOf(SunMiscUnsafeBridge.class, unsafe);
        Assertions.assertInstanceOf(Object.class, unsafe.allocateInstance(Object.class));
    }

}
