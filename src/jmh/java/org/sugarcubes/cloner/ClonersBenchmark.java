package org.sugarcubes.cloner;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.sugarcubes.cloner.other.FstCloner;
import org.sugarcubes.cloner.other.KryoCloner;
import org.sugarcubes.cloner.unsafe.UnsafeReflectionCloner;

/**
 * Benchmarks for cloners.
 *
 * @author Maxim Butov
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@SuppressWarnings("checkstyle:MagicNumber")
public class ClonersBenchmark {

    static class A implements Serializable {

        int a = 1;
        String b = "2";
        Object c = null;

        int[] d = new Random().ints().limit(1_000).toArray();
        Object[] e = new Random().ints().boxed().limit(1_000).toArray();

        Object[] f = {
            3, "4", 5.0, new int[] {6}, new HashMap<>(Collections.singletonMap(7, 8.0)),
        };
        Object[] g = new Object[1_000];

        {
            g[0] = this;
            g[1] = g;
        }
    }

    static class B extends A {

        Object a = this;

        B(int i) {
        }

    }

    SerializableCloner serializableCloner;
    ReflectionCloner reflectionCloner;
    UnsafeReflectionCloner unsafeReflectionCloner;
    KryoCloner kryoCloner;
    FstCloner fstCloner;

    Object sample;

    @Setup
    void setup() {
        serializableCloner = new SerializableCloner();
        reflectionCloner = new ReflectionCloner();
        unsafeReflectionCloner = new UnsafeReflectionCloner();
        kryoCloner = new KryoCloner();
        fstCloner = new FstCloner();

        sample = new B(1);
    }

    @Benchmark
    void serializable() {
        serializableCloner.clone(sample);
    }

    @Benchmark
    void reflection() {
        reflectionCloner.clone(sample);
    }

    @Benchmark
    void unsafe() {
        unsafeReflectionCloner.clone(sample);
    }

    @Benchmark
    void kryo() {
        kryoCloner.clone(sample);
    }

    @Benchmark
    void fst() {
        fstCloner.clone(sample);
    }

}
