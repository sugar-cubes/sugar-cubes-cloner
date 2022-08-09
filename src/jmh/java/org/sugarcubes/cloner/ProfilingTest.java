package org.sugarcubes.cloner;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.kryo.Kryo;

@SuppressWarnings("checkstyle:all")
public class ProfilingTest {

    private List<Object> objects;

    private Cloner serialization = new SerializationCloner();
    private Cloner reflection = new ReflectionCloner().parallel(ForkJoinPool.commonPool());
    private Cloner unsafe = new UnsafeCloner();

    private Kryo kryo = new Kryo();
    private com.rits.cloning.Cloner kk = new com.rits.cloning.Cloner();

    @BeforeEach
    public void setup() {

        objects = Stream.generate(() -> TestObjectFactory.randomObject(10, 8))
            .limit(100)
            .parallel()
            .collect(Collectors.toList());

        kryo.setRegistrationRequired(false);

        doTests();

    }

    @Test
    void doTests() {
        reflection();
        kryo();
        kk();
    }

    private void reflection() {
        measure("reflection", () -> objects.forEach(reflection::clone));
    }

    private void kryo() {
        measure("kryo", () -> objects.forEach(kryo::copy));
    }

    private void kk() {
        measure("kk", () -> objects.forEach(kk::deepClone));
    }

    private static void measure(String label, Runnable action) {
        long start = System.nanoTime();
        action.run();
        long end = System.nanoTime();
        System.out.println(label + " - " + Duration.ofNanos(end - start).toMillis() + " ms");
    }

}
