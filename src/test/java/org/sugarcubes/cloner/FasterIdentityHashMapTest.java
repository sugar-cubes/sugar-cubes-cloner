package org.sugarcubes.cloner;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

class FasterIdentityHashMapTest {

    @Test
    void testMap() {
        Map<Object, Object> ihm = new IdentityHashMap<>();
        Map<Object, Object> fihm = new FasterIdentityHashMap<>();

        List<Object> keys = Stream.generate(Object::new).limit(1000).collect(Collectors.toList());
        for (Object key : keys) {
            Object value = new Object();
            ihm.put(key, value) ;
            fihm.put(key, value) ;
        }

        assertThat(fihm.size(), is(ihm.size()));

        for (Object key : keys) {
            assertThat(fihm.get(key), sameInstance(ihm.get(key)));
        }
    }

}
