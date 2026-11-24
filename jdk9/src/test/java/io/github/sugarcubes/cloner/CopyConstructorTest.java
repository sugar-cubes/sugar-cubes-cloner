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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.sugarcubes.cloner.generation.CopyConstructors;

/**
 * Copy constructors written by hand in the form the build tools generate.
 */
class CopyConstructorTest {

    static final AtomicInteger CONSTRUCTED = new AtomicInteger();

    static class Base {

        final String name;

        Base(String name) {
            this.name = name;
        }

        protected Base(CopyConstructors marker, Base original, CopyContext context) throws Exception {
            super();
            CopyConstructors.register(context, this);
            CONSTRUCTED.incrementAndGet();
            this.name = (String) CopyConstructors.copy(context, original.name, this, Base.class, "name");
        }

    }

    static final class Node extends Base {

        final int value;

        final Node next;

        final List<Node> children;

        Node(String name, int value, Node next) {
            super(name);
            this.value = value;
            this.next = next;
            this.children = new ArrayList<>();
        }

        @SuppressWarnings("unchecked")
        private Node(CopyConstructors marker, Node original, CopyContext context) throws Exception {
            super(marker, original, context);
            this.value = original.value;
            this.next = (Node) CopyConstructors.copy(context, original.next, this, Node.class, "next");
            this.children = (List<Node>) CopyConstructors.copy(context, original.children, this, Node.class,
                "children");
        }

    }

    /**
     * Cycle through a final field: the clone of the head is being constructed when the tail refers to it.
     */
    static final class Ring {

        final int id;

        Ring next;

        final Ring first;

        Ring(int id, Ring first) {
            this.id = id;
            this.first = first != null ? first : this;
        }

        private Ring(CopyConstructors marker, Ring original, CopyContext context) throws Exception {
            super();
            CopyConstructors.register(context, this);
            this.id = original.id;
            this.next = (Ring) CopyConstructors.copy(context, original.next, this, Ring.class, "next");
            this.first = (Ring) CopyConstructors.copy(context, original.first, this, Ring.class, "first");
        }

    }

    @BeforeEach
    void reset() {
        CONSTRUCTED.set(0);
    }

    @ParameterizedTest
    @EnumSource(CloningMode.class)
    void clonesByCopyConstructor(CloningMode mode) {
        Node tail = new Node("tail", 2, null);
        Node head = new Node("head", 1, tail);
        head.children.add(tail);

        Node clone = Cloners.builder().mode(mode).build().clone(head);

        Assertions.assertEquals(2, CONSTRUCTED.get());
        Assertions.assertNotSame(head, clone);
        Assertions.assertEquals("head", clone.name);
        Assertions.assertEquals(1, clone.value);
        Assertions.assertEquals(2, clone.next.value);
        Assertions.assertSame(clone.next, clone.children.get(0));
    }

    @ParameterizedTest
    @EnumSource(CloningMode.class)
    void cycleThroughFinalFieldIsFilledLater(CloningMode mode) {
        Ring first = new Ring(1, null);
        Ring second = new Ring(2, first);
        first.next = second;
        second.next = first;

        Ring clone = Cloners.builder().mode(mode).build().clone(second);

        Assertions.assertNotSame(second, clone);
        Assertions.assertSame(clone, clone.next.next);
        Assertions.assertSame(clone.next, clone.first);
        Assertions.assertSame(clone.next, clone.next.first);
    }

    @ParameterizedTest
    @EnumSource(value = CloningMode.class, names = {"SEQUENTIAL", "PARALLEL"})
    void longChainDoesNotOverflowStack(CloningMode mode) {
        Node head = null;
        for (int k = 0; k < 100_000; k++) {
            head = new Node("n", k, head);
        }

        Node clone = Cloners.builder().mode(mode).build().clone(head);

        int count = 0;
        for (Node node = clone; node != null; node = node.next) {
            Assertions.assertEquals(99_999 - count, node.value);
            count++;
        }
        Assertions.assertEquals(100_000, count);
    }

    @Test
    void fieldPolicyFallsBackToFields() {
        Node original = new Node("node", 1, null);

        Node clone = Cloners.builder().fieldAction(Base.class, "name", CopyAction.NULL).build().clone(original);

        Assertions.assertEquals(0, CONSTRUCTED.get());
        Assertions.assertNull(clone.name);
        Assertions.assertEquals(1, clone.value);
    }

    @Test
    void canBeDisabled() {
        Node clone = Cloners.builder().copyConstructors(false).build().clone(new Node("node", 1, null));

        Assertions.assertEquals(0, CONSTRUCTED.get());
        Assertions.assertEquals("node", clone.name);
    }

}
