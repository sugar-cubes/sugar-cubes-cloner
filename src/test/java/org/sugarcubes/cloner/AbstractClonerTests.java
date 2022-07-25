package org.sugarcubes.cloner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Maxim Butov
 */
public abstract class AbstractClonerTests {

    static class A implements Serializable {

        final int x = 1;
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
        int n = 5;

        B(int i) {
        }

    }

    protected abstract Cloner getCloner();

    @Test
    public void testCloner() {

        Cloner cloner = getCloner();

        Assertions.assertNull(cloner.clone(null));

        B b1 = new B(1);
        B b2 = cloner.clone(b1);

        Assertions.assertEquals(b1.x, b2.x);
        Assertions.assertSame(b1.y, b2.y);
        Assertions.assertNull(b2.z);
        Assertions.assertSame(b2.a, b2);

        Assertions.assertTrue(Arrays.equals(b1.xxx, b2.xxx));
        Assertions.assertTrue(Arrays.deepEquals(b1.yyy, b2.yyy));
        Assertions.assertSame(b2, b2.zzz[0]);
        Assertions.assertSame(b2.zzz, b2.zzz[1]);

        Assertions.assertSame(b2, b2.a);
        Assertions.assertEquals(b1.n, b2.n);

    }

}
