package org.sugarcubes.cloner;

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
    private Kryo kryo;
    private com.rits.cloning.Cloner kk;

    private Object sample;

    @Setup
    public void setup() {

        serialization = new SerializationCloner();
        reflection = Cloners.builder().build();
        recursive = Cloners.builder().setMode(CloningMode.RECURSIVE).build();
        unsafe = Cloners.builder().setUnsafe().build();
        kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kk = new com.rits.cloning.Cloner();

//        sample = new Object();
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
    public void kryo() {
        kryo.copy(sample);
    }

    @Benchmark
    public void kk() {
        kk.deepClone(sample);
    }

}
