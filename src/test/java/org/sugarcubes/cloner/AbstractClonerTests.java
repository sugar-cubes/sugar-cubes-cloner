package org.sugarcubes.cloner;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

abstract class AbstractClonerTests {

    final Cloner cloner;

    AbstractClonerTests(Cloner cloner) {
        this.cloner = cloner;
    }

    @Test
    void testNull() {
        assertThat(cloner.clone(null), nullValue());
    }

    static class SimpleFields implements Serializable {

        int i;
        double d;
        String s;

    }

    @Test
    void testSimpleFields() {
        SimpleFields original = new SimpleFields();
        original.i = 1;
        original.d = 2.0;
        original.s = "str";
        SimpleFields clone = cloner.clone(original);
        assertThat(clone.i, is(original.i));
        assertThat(clone.d, is(original.d));
        assertThat(clone.s, is(original.s));
    }

    @Test
    void testArray() {
        Number[] original = {0, 1L, 2.0, BigInteger.valueOf(3), BigDecimal.valueOf(4.0)};
        Number[] clone = cloner.clone(original);
        assertThat(clone, not(sameInstance(original)));
        assertThat(clone.length, is(original.length));
        assertThat(clone[0], is(original[0]));
        assertThat(clone[1], is(original[1]));
        assertThat(clone[2], is(original[2]));
        assertThat(clone[3], is(original[3]));
        assertThat(clone[4], is(original[4]));
    }

    static class CyclicDependencies implements Serializable {

        Object self = this;
        Object[] array = new Object[2];
        List<Object> list = new ArrayList<>();
        Map<Object, Object> map = new HashMap<>();

        CyclicDependencies() {
            array[0] = this;
            array[1] = array;

            list.add(this);
            list.add(list);

            map.put("self", this);
            map.put("map", map);
        }

    }

    @Test
    void testCyclicDependencies() {
        CyclicDependencies original = new CyclicDependencies();
        CyclicDependencies clone = cloner.clone(original);

        assertThat(clone, not(sameInstance(original)));

        assertThat(clone.self, not(sameInstance(original.self)));
        assertThat(clone.self, sameInstance(clone));

        assertThat(clone.array, not(sameInstance(original.array)));
        assertThat(clone.array[0], sameInstance(clone));
        assertThat(clone.array[1], sameInstance(clone.array));

        assertThat(clone.list, not(sameInstance(original.list)));
        assertThat(clone.list.get(0), sameInstance(clone));
        assertThat(clone.list.get(1), sameInstance(clone.list));

        assertThat(clone.map, not(sameInstance(original.map)));
        assertThat(clone.map.get("self"), sameInstance(clone));
        assertThat(clone.map.get("map"), sameInstance(clone.map));
    }

    static class NoDefaultConstructor implements Serializable {

        final int i;

        NoDefaultConstructor(int i) {
            this.i = i;
        }
    }

    @Test
    void testNoDefaultConstructor() {
        NoDefaultConstructor original = new NoDefaultConstructor(1);
        NoDefaultConstructor clone = cloner.clone(original);

        assertThat(clone, not(sameInstance(original)));
        assertThat(clone.i, is(original.i));
    }

    @Test
    void testRandomObjects() {
        List<Object> objects = Stream.generate(() -> TestObjectFactory.randomObject(10, 10))
            .limit(10)
            .parallel()
            .collect(Collectors.toList());
        cloner.clone(objects);
    }

}
