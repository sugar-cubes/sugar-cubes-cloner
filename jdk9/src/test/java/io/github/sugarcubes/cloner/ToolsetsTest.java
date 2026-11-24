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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.sugarcubes.cloner.fields.ReflectionFieldAdapterImpl;
import io.github.sugarcubes.cloner.fields.UnsafeFieldAdapterImpl;
import io.github.sugarcubes.cloner.spi.FieldAdapterFactory;

class ToolsetsTest {

    static class Counted {

        static final AtomicInteger CONSTRUCTED = new AtomicInteger();

        private final int value;

        Counted(int value) {
            this.value = value;
            CONSTRUCTED.incrementAndGet();
        }

    }

    @Test
    void defaultsPreferUnsafe() throws Exception {
        Toolset toolset = Toolsets.defaults();

        // no agent and no Narcissus in this JVM
        Assertions.assertEquals("Toolset[moduleOpening=UNSAFE, allocation=[UNSAFE, REFLECTION_FACTORY], "
            + "constructorFallback=constructors, fieldAccess=[UNSAFE, REFLECTION]]", toolset.toString());
        Assertions.assertInstanceOf(UnsafeFieldAdapterImpl.class,
            toolset.getFieldAdapterFactory().newAdapter(Counted.class.getDeclaredField("value")).get());
    }

    @Test
    void conservativeIsBuilderDefaults() {
        Assertions.assertEquals(Toolsets.builder().build().toString(), Toolsets.conservative().toString());
    }

    @Test
    void builderDefaultsDoNotDependOnJvm() throws Exception {
        Toolset toolset = Toolsets.builder().build();

        Assertions.assertEquals("Toolset[moduleOpening=NONE, allocation=[REFLECTION_FACTORY], "
            + "constructorFallback=constructors, fieldAccess=[REFLECTION, UNSAFE if reflection cannot access]]",
            toolset.toString());
        Assertions.assertInstanceOf(ReflectionFieldAdapterImpl.class,
            toolset.getFieldAdapterFactory().newAdapter(Counted.class.getDeclaredField("value")).get());
    }

    @Test
    void unsafeProfile() throws Exception {
        Toolset toolset = Toolsets.unsafe();
        Counted original = new Counted(7);
        int constructed = Counted.CONSTRUCTED.get();

        Counted clone = Cloners.builder().toolset(toolset).build().clone(original);

        Assertions.assertEquals(7, clone.value);
        Assertions.assertEquals(constructed, Counted.CONSTRUCTED.get());
        Assertions.assertInstanceOf(UnsafeFieldAdapterImpl.class,
            toolset.getFieldAdapterFactory().newAdapter(Counted.class.getDeclaredField("value")).get());
    }

    @Test
    void reflectionProfileClones() {
        Counted clone = Cloners.builder().toolset(Toolsets.reflection()).build().clone(new Counted(3));

        Assertions.assertEquals(3, clone.value);
    }

    @Test
    void narcissusNotAvailable() {
        ClonerException error = Assertions.assertThrows(ClonerException.class, Toolsets::narcissus);

        Assertions.assertTrue(error.getMessage().contains("Narcissus"), error.getMessage());
    }

    @Test
    void instrumentationNeedsAgent() {
        ToolsetBuilder builder = Toolsets.builder().moduleOpening(ModuleOpening.INSTRUMENTATION);

        ClonerException error = Assertions.assertThrows(ClonerException.class, builder::build);

        Assertions.assertTrue(error.getMessage().contains("-javaagent"), error.getMessage());
    }

    @Test
    void customReflectionWithModuleOpeningIsMisconfiguration() {
        ToolsetBuilder builder = Toolsets.builder()
            .reflection(Toolsets.defaults().getReflection())
            .moduleOpening(ModuleOpening.UNSAFE);

        Assertions.assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void variantsAreCombined() {
        Toolset toolset = Toolsets.builder()
            .allocation(Allocation.UNSAFE, Allocation.REFLECTION_FACTORY)
            .fieldAccess(FieldAccess.REFLECTION, FieldAccess.UNSAFE)
            .moduleOpening(ModuleOpening.UNSAFE)
            .build();

        Counted clone = Cloners.builder().toolset(toolset).build().clone(new Counted(5));

        Assertions.assertEquals(5, clone.value);
    }

    @Test
    void variantsAndCustomAdaptersAreMixedInOrder() throws Exception {
        Field value = Counted.class.getDeclaredField("value");
        List<String> asked = new ArrayList<>();
        FieldAdapterFactory first = field -> {
            asked.add("first");
            return Optional.empty();
        };
        Toolset toolset = Toolsets.builder()
            .fieldAdapter(first)
            .fieldAccess(FieldAccess.UNSAFE)
            .fieldAdapter(field -> {
                asked.add("last");
                return Optional.empty();
            })
            .build();

        Assertions.assertInstanceOf(UnsafeFieldAdapterImpl.class,
            toolset.getFieldAdapterFactory().newAdapter(value).get());
        Assertions.assertEquals(Collections.singletonList("first"), asked);
    }

    @Test
    void emptyAllocatorsDisableDefaults() {
        Toolset toolset = Toolsets.builder().allocators().build();

        Assertions.assertTrue(toolset.toString().contains("allocation=[]"), toolset.toString());
    }

    @Test
    void toStringDescribesMechanisms() {
        Toolset toolset = Toolsets.builder()
            .moduleOpening(ModuleOpening.UNSAFE)
            .allocation(Allocation.UNSAFE)
            .fieldAccess(FieldAccess.VAR_HANDLE, FieldAccess.REFLECTION)
            .noConstructorFallback()
            .build();

        Assertions.assertEquals("Toolset[moduleOpening=UNSAFE, allocation=[UNSAFE], constructorFallback=none, "
            + "fieldAccess=[VAR_HANDLE, REFLECTION]]", toolset.toString());
    }

    @Test
    void availability() {
        Assertions.assertTrue(Toolsets.isAvailable(Allocation.UNSAFE));
        Assertions.assertTrue(Toolsets.isAvailable(Allocation.REFLECTION_FACTORY));
        Assertions.assertTrue(Toolsets.isAvailable(FieldAccess.VAR_HANDLE));
        Assertions.assertTrue(Toolsets.isAvailable(ModuleOpening.UNSAFE));
        Assertions.assertFalse(Toolsets.isAvailable(FieldAccess.NARCISSUS));
        Assertions.assertFalse(Toolsets.isAvailable(ModuleOpening.NARCISSUS));
        Assertions.assertFalse(Toolsets.isAvailable(ModuleOpening.INSTRUMENTATION));
    }

}
