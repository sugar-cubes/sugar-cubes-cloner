package org.sugarcubes.cloner;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sugarcubes.cloner.other.FstCloner;
import org.sugarcubes.cloner.other.KKCloner;

public class ProfilingTest {

    private List<Object> objects;

    private Cloner serializable = new SerializableCloner();
    private Cloner reflection = new ReflectionCloner();
    private Cloner kk = new KKCloner();
    private Cloner fst = new FstCloner();

    @BeforeEach
    public void setup() {
        objects = Stream.generate(() -> TestObjectFactory.randomObject(8, 8))
            .limit(16)
            .collect(Collectors.toList());
        objects.forEach(serializable::clone);
        objects.forEach(reflection::clone);
        objects.forEach(kk::clone);
        objects.forEach(fst::clone);
    }

    @Test
    void doTests() {
        objects.forEach(serializable::clone);
        objects.forEach(reflection::clone);
        objects.forEach(kk::clone);
        objects.forEach(fst::clone);
    }

}
