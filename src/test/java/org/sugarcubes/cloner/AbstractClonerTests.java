package org.sugarcubes.cloner;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

/**
 * @author Maxim Butov
 */
public abstract class AbstractClonerTests {

    static int counter = 0;

    static class A implements Serializable {

        final int x = ++counter;
        final String y = "2";
        final Object z = null;

        byte[] xxx = {1, 2, 3,};
        Object[] yyy = {1, "2", 3.0, new int[] {4}, Collections.singletonMap(5, 5.0),};
        Object[] zzz = new Object[2];

        {
            zzz[0] = this;
            zzz[1] = zzz;
        }

    }

    static class B extends A {

        A a = this;
        int b = 5;
        Map c = new HashMap();

        B(int i) {
            c.put("c", c);
        }

    }

    protected abstract Cloner getCloner();

    @Test
    void testCloner() {
        Cloner cloner = getCloner();

        assertThat(cloner.clone(null), nullValue());

        B b1 = new B(1);
        B b2 = cloner.clone(b1);

        assertThat(b2.x, is(b1.x));
        assertThat(b2.y, sameInstance(b1.y));
        assertThat(b2.z, nullValue());
        assertThat(b2.a, sameInstance(b2));
        assertThat(b2.b, is(b1.b));

        assertThat(b2.xxx, is(b1.xxx));
        assertThat(b2.yyy, is(b1.yyy));
        assertThat(b2.zzz[0], sameInstance(b2));
        assertThat(b2.zzz[1], sameInstance(b2.zzz));
    }

    @Test
    void testRandomObjects() throws Exception {
        Cloner cloner = getCloner();
        for (int k = 0; k < 10; k++) {
            Object original = TestObjectFactory.randomObject(false, 10, 10);
            Object clone = cloner.clone(original);
            assertThat(clone, notNullValue());
        }
    }

}
