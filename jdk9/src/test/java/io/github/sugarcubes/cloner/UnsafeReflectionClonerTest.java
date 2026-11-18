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
package io.github.sugarcubes.cloner;

import io.github.sugarcubes.cloner.internal.JdkPlatform;
import io.github.sugarcubes.cloner.internal.ObjenesisInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.UnsafeBridge;
import io.github.sugarcubes.cloner.internal.UnsafeFieldAdapterImpl;
import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.spi.TypeOpener;

class UnsafeReflectionClonerTest extends AbstractClonerTests {

    public UnsafeReflectionClonerTest() {
        super(unsafeCloner());
    }

    private static Cloner unsafeCloner() {
        Reflection reflection = JdkPlatform.current().reflection(TypeOpener.NOOP);
        return Cloners.builder().toolset(new Toolset(reflection, new ObjenesisInstanceAllocatorFactory(),
            UnsafeFieldAdapterImpl.factory(UnsafeBridge.getInstance(reflection)))).build();
    }

}
