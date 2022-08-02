package org.sugarcubes.cloner;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.sugarcubes.cloner.thirdparty.FstCloner;
import org.sugarcubes.cloner.thirdparty.KKCloner;
import org.sugarcubes.cloner.thirdparty.KryoCloner;
import org.sugarcubes.cloner.unsafe.UnsafeReflectionCloner;

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

    private Cloner serializableCloner;
    private Cloner reflectionCloner;
    private Cloner unsafeReflectionCloner;
    private Cloner kryoCloner;
    private Cloner fstCloner;
    private Cloner kkCloner;

    private Object sample;

    @Setup
    public void setup() {

        serializableCloner = new SerializableCloner();
        reflectionCloner = new ReflectionCloner();
        unsafeReflectionCloner = new UnsafeReflectionCloner();
        kryoCloner = new KryoCloner();
        fstCloner = new FstCloner();
        kkCloner = new KKCloner();

        sample = TestObjectFactory.randomObject(false, 10, 10);
    }

    @Benchmark
    public void serializable() {
        serializableCloner.clone(sample);
    }

    @Benchmark
    public void reflection() {
        reflectionCloner.clone(sample);
    }

    @Benchmark
    public void unsafe() {
        unsafeReflectionCloner.clone(sample);
    }

    @Benchmark
    public void kryo() {
        kryoCloner.clone(sample);
    }

    @Benchmark
    public void fst() {
        fstCloner.clone(sample);
    }

    @Benchmark
    public void kk() {
        kkCloner.clone(sample);
    }

}
