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
package io.github.sugarcubes.cloner;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.sugarcubes.cloner.allocation.CompoundInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.allocation.ReflectionFactoryAllocatorFactory;
import io.github.sugarcubes.cloner.fields.ReflectionFieldAdapterImpl;
import io.github.sugarcubes.cloner.platform.JdkPlatform;
import io.github.sugarcubes.cloner.spi.InstanceAllocator;
import io.github.sugarcubes.cloner.spi.InstanceAllocatorFactory;

class ToolsetBuilderTest {

    @Test
    void defaultsAreFailsafe() throws Exception {
        Toolset toolset = Toolsets.builder().build();

        Assertions.assertSame(JdkPlatform.current().lowLevelAccess().reflection().getClass(), toolset.getReflection().getClass());
        Assertions.assertInstanceOf(ReflectionFieldAdapterImpl.class,
            toolset.getFieldAdapterFactory().newAdapter(Date.class.getDeclaredField("fastTime")).get());
        // ReflectionFactory first, constructor is only a fallback: Date() would set the current time
        Assertions.assertTrue(toolset.getAllocatorFactory().invokesConstructor());
        Assertions.assertEquals(0, toolset.getAllocatorFactory().newAllocator(Date.class).get().newInstance().getTime());
    }

    @Test
    void singleAllocatorIsUsedAsIs() {
        InstanceAllocatorFactory allocator = new ReflectionFactoryAllocatorFactory();

        Toolset toolset = Toolsets.builder().allocator(allocator).noConstructorFallback().build();

        Assertions.assertSame(allocator, toolset.getAllocatorFactory());
    }

    @Test
    void severalAllocatorsUseFirstSupportingOne() throws Exception {
        InstanceAllocatorFactory unsupporting = new InstanceAllocatorFactory() {
            @Override
            public <T> Optional<InstanceAllocator<T>> newAllocator(Class<T> type) {
                return Optional.empty();
            }
        };

        Toolset toolset = Toolsets.builder()
            .allocators(unsupporting, new ReflectionFactoryAllocatorFactory())
            .noConstructorFallback()
            .build();

        Assertions.assertInstanceOf(CompoundInstanceAllocatorFactory.class, toolset.getAllocatorFactory());
        Assertions.assertInstanceOf(Date.class, toolset.getAllocatorFactory().newAllocator(Date.class).get().newInstance());
    }

    @Test
    void builtToolsetClones() {
        Cloner cloner = Cloners.builder().toolset(Toolsets.builder().build()).build();
        Date original = new Date();

        Date clone = cloner.clone(original);

        Assertions.assertNotSame(original, clone);
        Assertions.assertEquals(original, clone);
    }

}
