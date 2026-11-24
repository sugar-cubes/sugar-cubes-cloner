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
package io.github.sugarcubes.cloner.generated;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.sugarcubes.cloner.Cloner;
import io.github.sugarcubes.cloner.Cloners;
import io.github.sugarcubes.cloner.CloningMode;
import io.github.sugarcubes.cloner.CopyAction;
import io.github.sugarcubes.cloner.CopyPolicy;
import io.github.sugarcubes.cloner.ObjectCopier;
import io.github.sugarcubes.cloner.ReflectionClonerBuilder;
import io.github.sugarcubes.cloner.Toolsets;
import io.github.sugarcubes.cloner.engine.FieldsCopier;
import io.github.sugarcubes.cloner.engine.ReflectionCopierProvider;
import io.github.sugarcubes.cloner.generated.model.Node;
import io.github.sugarcubes.cloner.generated.model.Sample;
import io.github.sugarcubes.cloner.generation.CopierGenerator;
import io.github.sugarcubes.cloner.generation.GeneratedFieldsCopier;

/**
 * Generated copiers of the classes listed in META-INF/sugar-cubes-cloner/generated-copiers of the test resources.
 */
class GeneratedCopierTest {

    private static ObjectCopier<Object> copierOf(Object original, boolean generated) {
        ReflectionCopierProvider provider = new ReflectionCopierProvider(Toolsets.defaults(), null,
            CopyPolicy.defaultPolicy(), CopyPolicy.defaultPolicy(), Collections.emptyMap(), Collections.emptySet(),
            generated);
        return provider.getCopier(original);
    }

    private static ReflectionClonerBuilder generating() {
        return Cloners.builder().generatedCopiers(true);
    }

    @Test
    void copiersAreGeneratedIfEnabled() {
        Assertions.assertTrue(CopierGenerator.instance().isPresent(), "ASM is on the test classpath");

        ObjectCopier<Object> copier = copierOf(new Sample(1, null), true);

        Assertions.assertInstanceOf(GeneratedFieldsCopier.class, copier);
        Assertions.assertTrue(copier.getClass().isHidden());
        Assertions.assertSame(Sample.class, copier.getClass().getNestHost());
        // finalInt and finalLong are written by setters from the class data, finalList and finalPartner get clones
        Assertions.assertEquals(2, Arrays.stream(copier.getClass().getDeclaredFields())
            .filter(field -> field.getName().startsWith("$setter"))
            .count());
        Assertions.assertInstanceOf(FieldsCopier.class, copierOf(new Sample(1, null), false));
    }

    @Test
    void clonersShareGeneratedClasses() {
        Sample original = new Sample(1, null);
        // a living copier keeps its class in the shared cache
        ObjectCopier<Object> copier = copierOf(original, true);
        Class<?> generated = copier.getClass();
        ReflectionCopierProvider skippingText = new ReflectionCopierProvider(Toolsets.defaults(), null,
            CopyPolicy.defaultPolicy(), field -> field.getName().equals("text") ? CopyAction.SKIP : CopyAction.DEFAULT,
            Collections.emptyMap(), Collections.emptySet(), true);

        Class<?> another = skippingText.getCopier(original).getClass();

        // another cloner with the same policies gets the same class, another policy gets another one
        Assertions.assertSame(generated, copierOf(original, true).getClass());
        Assertions.assertNotSame(generated, another);
        Assertions.assertTrue(another.isHidden());
        Assertions.assertNotNull(copier);
    }

    @ParameterizedTest
    @EnumSource(CloningMode.class)
    void allKindsOfFields(CloningMode mode) {
        Sample partner = new Sample(2, null);
        Sample original = new Sample(1, partner);
        original.partner = partner;
        partner.partner = original;
        original.samples = new Sample[] {partner, original};

        Sample clone = generating().mode(mode).build().clone(original);

        Assertions.assertNotSame(original, clone);
        Assertions.assertTrue(clone.z);
        Assertions.assertEquals(1, clone.b);
        Assertions.assertEquals('c', clone.c);
        Assertions.assertEquals(2, clone.s);
        Assertions.assertEquals(3, clone.i);
        Assertions.assertEquals(4, clone.l);
        Assertions.assertEquals(5.5f, clone.f);
        Assertions.assertEquals(6.5, clone.d);
        Assertions.assertEquals(1, clone.getFinalInt());
        Assertions.assertEquals(100, clone.getFinalLong());
        Assertions.assertEquals(List.of("item1"), clone.getFinalList());
        Assertions.assertNotSame(original.getFinalList(), clone.getFinalList());
        Assertions.assertEquals(10, clone.getBaseValue());
        Assertions.assertEquals("base1", clone.getBaseName());
        Assertions.assertEquals("text", clone.text);
        Assertions.assertEquals(List.of("x"), clone.any);
        Assertions.assertNotSame(original.any, clone.any);
        Assertions.assertArrayEquals(new int[] {1, 2, 3}, clone.ints);
        Assertions.assertNull(clone.nothing);
        // references inside the graph point to the clones
        Assertions.assertSame(clone, clone.self);
        Sample partnerClone = clone.partner;
        Assertions.assertNotSame(partner, partnerClone);
        Assertions.assertSame(clone, partnerClone.partner);
        Assertions.assertSame(partnerClone, clone.getFinalPartner());
        Assertions.assertSame(partnerClone, clone.samples[0]);
        Assertions.assertSame(clone, clone.samples[1]);
    }

    @ParameterizedTest
    @EnumSource(CloningMode.class)
    void cycleThroughFinalField(CloningMode mode) {
        Node root = new Node(null, "root");
        new Node(root, "a");
        new Node(root, "b");

        Node clone = generating().mode(mode).build().clone(root);

        Assertions.assertEquals(2, clone.getChildren().size());
        for (Node child : clone.getChildren()) {
            Assertions.assertSame(clone, child.getParent());
        }
        Assertions.assertEquals("b", clone.getChildren().get(1).getName());
    }

    @Test
    void fieldPolicies() {
        Sample original = new Sample(1, null);
        original.partner = new Sample(2, null);
        Cloner cloner = generating()
            .fieldAction(Sample.class, "text", CopyAction.SKIP)
            .fieldAction(Sample.class, "any", CopyAction.NULL)
            .fieldAction(Sample.class, "partner", CopyAction.ORIGINAL)
            .fieldAction(Sample.class, "finalList", CopyAction.ORIGINAL)
            .build();

        Sample clone = cloner.clone(original);

        Assertions.assertNull(clone.text);
        Assertions.assertNull(clone.any);
        Assertions.assertSame(original.partner, clone.partner);
        Assertions.assertSame(original.getFinalList(), clone.getFinalList());
        Assertions.assertEquals(3, clone.i);
    }

    @Test
    void shallowCopy() {
        Sample original = new Sample(1, null);

        Sample clone = generating().shallow(Sample.class).build().clone(original);

        Assertions.assertNotSame(original, clone);
        Assertions.assertSame(original.any, clone.any);
        Assertions.assertSame(original.getFinalList(), clone.getFinalList());
        Assertions.assertSame(original, clone.self);
    }

    @Test
    void unusedClassesAreUnloaded() throws InterruptedException {
        WeakReference<Class<?>> generated = new WeakReference<>(copierOf(new Sample(1, null), true).getClass());

        // no cloner holds a copier: the shared cache does not keep the class
        for (int i = 0; i < 50 && generated.get() != null; i++) {
            System.gc();
            Thread.sleep(20);
        }

        Assertions.assertNull(generated.get());
        Assertions.assertInstanceOf(GeneratedFieldsCopier.class, copierOf(new Sample(1, null), true));
    }

}
