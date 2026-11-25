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

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class UncopyableObjectTest {

    static class Task {

        String name = "task";

        Thread worker = Thread.currentThread();

    }

    static class Job {

        List<Task> tasks = new ArrayList<>(Collections.singletonList(new Task()));

    }

    static class Node {

        Node next;

        Object value;

    }

    static class Resources {

        Object resource;

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        ThreadLocal<String> local = new ThreadLocal<>();

        Resources(Object resource) {
            this.resource = resource;
            buffer.write(42);
        }

    }

    @ParameterizedTest
    @EnumSource(CloningMode.class)
    void threadFailsByDefault(CloningMode mode) {
        Cloner cloner = Cloners.builder().mode(mode).build();

        UncopyableObjectException error = Assertions.assertThrows(UncopyableObjectException.class,
            () -> cloner.clone(new Job()));

        Assertions.assertEquals(Thread.class, error.getType());
        Assertions.assertTrue(error.getMessage().startsWith("Cannot copy java.lang.Thread in " + Task.class.getName()),
            error.getMessage());
        Assertions.assertTrue(error.getMessage().contains("CopyAction.ORIGINAL"), error.getMessage());
    }

    @Test
    void holdersAreListedInRecursiveMode() {
        Cloner cloner = Cloners.builder().mode(CloningMode.RECURSIVE).build();

        UncopyableObjectException error = Assertions.assertThrows(UncopyableObjectException.class,
            () -> cloner.clone(new Job()));

        Assertions.assertTrue(error.getMessage().startsWith(String.format("Cannot copy java.lang.Thread in %s in %s in %s",
            Task.class.getName(), ArrayList.class.getName(), Job.class.getName())), error.getMessage());
    }

    @Test
    void repeatedHoldersAreCollapsed() {
        Node head = new Node();
        Node node = head;
        for (int k = 0; k < 100; k++) {
            node.next = new Node();
            node = node.next;
        }
        node.value = Thread.currentThread();
        Cloner cloner = Cloners.builder().mode(CloningMode.RECURSIVE).build();

        UncopyableObjectException error = Assertions.assertThrows(UncopyableObjectException.class,
            () -> cloner.clone(head));

        Assertions.assertTrue(error.getMessage().startsWith("Cannot copy java.lang.Thread in " + Node.class.getName()
            + ": "), error.getMessage());
    }

    @Test
    void typeActionShares() {
        Task original = new Task();

        Task clone = Cloners.builder().typeAction(Thread.class, CopyAction.ORIGINAL).build().clone(original);

        Assertions.assertNotSame(original, clone);
        Assertions.assertSame(original.worker, clone.worker);
    }

    @Test
    void fieldActionNulls() {
        Task clone = Cloners.builder().fieldAction(Task.class, "worker", CopyAction.CLEAR).build().clone(new Task());

        Assertions.assertNull(clone.worker);
        Assertions.assertEquals("task", clone.name);
    }

    @Test
    void objectActionShares() {
        Task original = new Task();

        Task clone = Cloners.builder().objectAction(original.worker, CopyAction.ORIGINAL).build().clone(original);

        Assertions.assertSame(original.worker, clone.worker);
    }

    @Test
    void shallowModeDoesNotCopy() {
        Cloner cloner = Cloners.builder().shallow(Thread.class).build();

        Assertions.assertThrows(UncopyableObjectException.class, () -> cloner.clone(new Task()));
    }

    @Test
    void nullValueIsFine() {
        Task original = new Task();
        original.worker = null;

        Assertions.assertNull(Cloners.clone(original).worker);
    }

    @Test
    void resourcesFailByDefault() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            for (Object resource : new Object[] {FileDescriptor.in, new WeakReference<>(new Object()), executor}) {
                UncopyableObjectException error = Assertions.assertThrows(UncopyableObjectException.class,
                    () -> Cloners.clone(new Resources(resource)));

                Assertions.assertTrue(error.getType().isInstance(resource), error.getMessage());
            }
        }
        finally {
            executor.shutdown();
        }
    }

    @Test
    void streamsInMemoryAreCopiedAndThreadLocalsShared() {
        Resources original = new Resources("text");
        original.local.set("value");

        Resources clone = Cloners.clone(original);

        Assertions.assertNotSame(original.buffer, clone.buffer);
        Assertions.assertArrayEquals(original.buffer.toByteArray(), clone.buffer.toByteArray());
        Assertions.assertSame(original.local, clone.local);
        Assertions.assertEquals("value", clone.local.get());
    }

}
