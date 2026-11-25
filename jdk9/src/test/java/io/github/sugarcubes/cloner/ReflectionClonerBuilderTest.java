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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ReflectionClonerBuilderTest {

    static class Item {

        String name;

        Item next;

        Item(String name, Item next) {
            this.name = name;
            this.next = next;
        }

    }

    @Test
    void executorEnablesParallelMode() throws InterruptedException {
        AtomicInteger tasks = new AtomicInteger();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>()) {
            @Override
            protected void beforeExecute(Thread thread, Runnable task) {
                tasks.incrementAndGet();
            }
        };
        try {
            List<Item> original = new ArrayList<>();
            for (int k = 0; k < 10; k++) {
                original.add(new Item("item" + k, new Item("next" + k, null)));
            }

            List<Item> clone = Cloners.builder().executor(executor).build().clone(original);

            Assertions.assertEquals("next9", clone.get(9).next.name);
            Assertions.assertNotSame(original.get(9).next, clone.get(9).next);
            Assertions.assertTrue(tasks.get() > 0);
        }
        finally {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.SECONDS);
        }
    }

}
