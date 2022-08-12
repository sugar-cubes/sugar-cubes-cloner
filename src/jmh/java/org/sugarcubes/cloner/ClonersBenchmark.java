package org.sugarcubes.cloner;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Benchmarks for cloners.
 *
 * @author Maxim Butov
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@SuppressWarnings("checkstyle:all")
public class ClonersBenchmark {

    private Cloner serialization;
    private Cloner reflection;
    private Cloner unsafe;

    private Object sample;

    @Setup
    public void setup() {

        serialization = new SerializationCloner();
        reflection = new ReflectionClonerBuilder().build();
        unsafe = new ReflectionClonerBuilder().setUnsafeEnabled().build();

        sample = TestObjectFactory.randomObject(10, 10);
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
    public void unsafe() {
        unsafe.clone(sample);
    }

}
