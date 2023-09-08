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
package io.github.sugarcubes.cloner;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import com.esotericsoftware.kryo.Kryo;

/**
 * Benchmarks for cloners.
 *
 * @author Maxim Butov
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1)
@Warmup(iterations = 1, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@SuppressWarnings("checkstyle:all")
public class ClonersBenchmark {

    private Cloner serialization;
    private Cloner reflection;
    private Cloner recursive;
    private Cloner unsafe;
    private Cloner parallel;
    private Cloner varhandle;
    private Kryo kryo;
    private com.rits.cloning.Cloner kk;

    private Object sample;

    @Setup
    public void setup() {

        serialization = Cloners.serialization();
        reflection = Cloners.builder().build();
        recursive = Cloners.builder().mode(CloningMode.RECURSIVE).build();
        unsafe = Cloners.builder().unsafe().build();
        parallel = Cloners.builder().mode(CloningMode.PARALLEL).build();
        varhandle = Cloners.builder().fieldCopierFactory(new VarHandleFieldCopierFactory()).build();
        kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kk = new com.rits.cloning.Cloner();

        sample = Stream.generate(() -> TestObjectFactory.randomObject(10, 10))
            .limit(10)
            .collect(Collectors.toList());

    }

    @Benchmark
    public void serialization() {
        serialization.clone(sample);
    }

    @Benchmark
    public void reflection() {
        reflection.clone(sample);
    }

    @Benchmark
    public void recursive() {
        recursive.clone(sample);
    }

    @Benchmark
    public void unsafe() {
        unsafe.clone(sample);
    }

    @Benchmark
    public void parallel() {
        parallel.clone(sample);
    }

    @Benchmark
    public void varhandle() {
        varhandle.clone(sample);
    }

    @Benchmark
    public void kryo() {
        kryo.copy(sample);
    }

    @Benchmark
    public void kk() {
        kk.deepClone(sample);
    }

}
