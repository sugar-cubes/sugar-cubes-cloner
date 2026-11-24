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
package io.github.sugarcubes.cloner.generation;

import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.sugarcubes.cloner.Cloners;
import io.github.sugarcubes.cloner.CopyPolicy;
import io.github.sugarcubes.cloner.Toolset;
import io.github.sugarcubes.cloner.Toolsets;
import io.github.sugarcubes.cloner.TwoPhaseObjectCopier;
import io.github.sugarcubes.cloner.generation.model.Item;
import io.github.sugarcubes.cloner.platform.JdkPlatform;
import io.github.sugarcubes.cloner.spi.FieldAdapter;
import io.github.sugarcubes.cloner.spi.FieldsModel;
import io.github.sugarcubes.cloner.spi.InstanceAllocator;

/**
 * On JDK 24+ copiers are written by the ClassFile API; its classes and the ones of ASM copy the same.
 */
class ClassFileBackendTest {

    @Test
    void classFileApiIsTheBackend() {
        BytecodeBackend backend = CopierGenerator.instance().orElseThrow().getBackend();

        Assertions.assertEquals("ClassFileBytecodeBackend", backend.getClass().getSimpleName());
    }

    @Test
    void graphIsCloned() {
        Item owner = new Item(1, "owner", 1.5, null);
        Item item = new Item(2, "item", 2.5, owner);
        owner.setNext(item);
        item.setNext(owner);

        Item clone = Cloners.builder().generatedCopiers(true).build().clone(item);

        Assertions.assertEquals(2, clone.getId());
        Assertions.assertEquals("item", clone.getName());
        Assertions.assertEquals(2.5, clone.getPrice());
        Assertions.assertEquals(List.of("tag2"), clone.getTags());
        Assertions.assertNotSame(item.getTags(), clone.getTags());
        Assertions.assertSame(clone.getOwner(), clone.getNext());
        Assertions.assertSame(clone, clone.getOwner().getNext());
        Assertions.assertEquals("owner", clone.getOwner().getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void backendsWriteEquivalentCopiers() throws Throwable {
        Toolset toolset = Toolsets.defaults();
        FieldsModel<Item> model = new FieldsModel<>(Item.class, toolset.getReflection(),
            toolset.getAllocatorFactory(), toolset.getFieldAdapterFactory());
        CopierTemplate template = new CopierTemplate(model, CopyPolicy.defaultPolicy(), false);
        Item original = new Item(3, "original", 3.5, null);

        for (BytecodeBackend backend : new BytecodeBackend[] {new AsmBytecodeBackend(),
            JdkPlatform.current().classDefiner().classFileBackend().orElseThrow()}) {
            MethodType constructor = MethodType.methodType(void.class, InstanceAllocator.class,
                TwoPhaseObjectCopier.class, FieldAdapter[].class);
            TwoPhaseObjectCopier<Item> copier = (TwoPhaseObjectCopier<Item>) JdkPlatform.current()
                .classDefiner().defineNestmate(Item.class, template.write(backend, 59), GeneratedFieldsCopier.class, constructor, null)
                .invoke(model.getAllocator().orElseThrow(), null, template.adapters());

            Item clone = Cloners.builder().copier(Item.class, copier).build().clone(original);

            Assertions.assertEquals(3, clone.getId(), backend.toString());
            Assertions.assertEquals("original", clone.getName(), backend.toString());
            Assertions.assertEquals(3.5, clone.getPrice(), backend.toString());
            Assertions.assertEquals(List.of("tag3"), clone.getTags(), backend.toString());
            Assertions.assertNotSame(original.getTags(), clone.getTags(), backend.toString());
        }
    }

}
