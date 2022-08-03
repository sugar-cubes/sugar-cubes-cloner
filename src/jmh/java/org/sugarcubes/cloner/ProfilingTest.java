package org.sugarcubes.cloner;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("checkstyle:all")
public class ProfilingTest {

    private List<Object> objects;

    private Cloner serialization = new SerializationCloner();
    private Cloner reflection = new ReflectionCloner();
    private Cloner unsafe = new UnsafeCloner();

    @BeforeEach
    public void setup() {

        objects = Stream.generate(() -> TestObjectFactory.randomObject(10, 8))
            .limit(100)
            .collect(Collectors.toList());
        
        doTests();
        
    }

    @Test
    void doTests() {
//        objects.forEach(serialization::clone);
//        objects.forEach(unsafe::clone);
        reflection();
    }

    private void reflection() {
        objects.forEach(reflection::clone);
    }

}
