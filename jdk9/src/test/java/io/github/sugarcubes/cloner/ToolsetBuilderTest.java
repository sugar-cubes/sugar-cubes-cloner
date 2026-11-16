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

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.sugarcubes.cloner.internal.CompoundInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.InstanceAllocator;
import io.github.sugarcubes.cloner.internal.InstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.ObjenesisInstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.ReflectionImpl$Jdk8;

class ToolsetBuilderTest {

    @Test
    void defaultsAreFailsafe() {
        Toolset toolset = new ToolsetBuilder().build();

        Assertions.assertInstanceOf(ReflectionImpl$Jdk8.class, toolset.getReflection());
        Assertions.assertInstanceOf(ObjenesisInstanceAllocatorFactory.class, toolset.getAllocatorFactory());
        Assertions.assertInstanceOf(ReflectionFieldCopierFactory.class, toolset.getFieldCopierFactory());
    }

    @Test
    void singleAllocatorIsUsedAsIs() {
        InstanceAllocatorFactory allocator = new ObjenesisInstanceAllocatorFactory();

        Toolset toolset = new ToolsetBuilder().allocator(allocator).build();

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

        Toolset toolset = new ToolsetBuilder().allocators(unsupporting, new ObjenesisInstanceAllocatorFactory()).build();

        Assertions.assertInstanceOf(CompoundInstanceAllocatorFactory.class, toolset.getAllocatorFactory());
        Assertions.assertInstanceOf(Date.class, toolset.getAllocatorFactory().newAllocator(Date.class).get().newInstance());
    }

    @Test
    void builtToolsetClones() {
        Cloner cloner = Cloners.builder().toolset(new ToolsetBuilder().build()).build();
        Date original = new Date();

        Date clone = cloner.clone(original);

        Assertions.assertNotSame(original, clone);
        Assertions.assertEquals(original, clone);
    }

}
