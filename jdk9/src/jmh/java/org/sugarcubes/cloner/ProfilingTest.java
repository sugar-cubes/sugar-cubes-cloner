/*
 * Copyright 2017-2023 the original author or authors.
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
package org.sugarcubes.cloner;

import java.time.Duration;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.kryo.Kryo;

/**
 * Test for profiling cloners.
 *
 * @author Maxim Butov
 */
class ProfilingTest {

    private final Cloner reflection = Cloners.builder().build();
    private final Cloner bfs = Cloners.builder().setTraversalAlgorithm(TraversalAlgorithm.BREADTH_FIRST).build();
    private final Cloner unsafe = Cloners.builder().setUnsafe().build();
    private final Cloner parallel = Cloners.builder().setMode(CloningMode.PARALLEL).build();
    private final Cloner recursive = Cloners.builder().setMode(CloningMode.RECURSIVE).build();
    private final Cloner varhandle = Cloners.builder().setFieldCopierFactory(new VarHandleFieldCopierFactory()).build();

    private final Kryo kryo = new Kryo();
    private final com.rits.cloning.Cloner kk = new com.rits.cloning.Cloner();

    private final Object sample = Stream.generate(() -> TestObjectFactory.randomObject(10, 10))
        .limit(10)
        .collect(Collectors.toList());

    public ProfilingTest() {
        kryo.setRegistrationRequired(false);
    }

    @Test
    void testCloners() {
        reflection();
        bfs();
        unsafe();
        parallel();
        recursive();
        varhandle();
        kryo();
        kk();
    }

    void reflection() {
        measure("reflection", () -> reflection.clone(sample));
    }

    void bfs() {
        measure("bfs", () -> bfs.clone(sample));
    }

    void unsafe() {
        measure("unsafe", () -> unsafe.clone(sample));
    }

    void parallel() {
        measure("parallel", () -> parallel.clone(sample));
    }

    void recursive() {
        measure("recursive", () -> recursive.clone(sample));
    }

    void varhandle() {
        measure("varhandle", () -> varhandle.clone(sample));
    }

    void kryo() {
        measure("kryo", () -> kryo.copy(sample));
    }

    void kk() {
        measure("kk", () -> kk.deepClone(sample));
    }

    void measure(String name, Runnable action) {
        long start = System.nanoTime();
        for (int k = 0; k < 10; k++) {
            action.run();
        }
        System.out.printf("%s - %s ms\n", name, Duration.ofNanos(System.nanoTime() - start).toMillis());
    }

}
