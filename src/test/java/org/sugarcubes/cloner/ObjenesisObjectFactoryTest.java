package org.sugarcubes.cloner;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ObjenesisObjectFactoryTest {

    public static class TestCase implements Callable {

        @Override
        public Object call() throws Exception {
            return new ObjenesisAllocator();
        }

    }

    @Test
    public void testNoObjenesis() throws Throwable {

        CustomClassLoader cl = new CustomClassLoader().disable("org.objenesis.").reload("org.sugarcubes.");
        Class<?> objenesisUtilsClass = cl.loadClass(ObjenesisUtils.class.getName());
        Method isObjenesisAvailableMethod = objenesisUtilsClass.getDeclaredMethod("isObjenesisAvailable");

        Assertions.assertEquals(false, isObjenesisAvailableMethod.invoke(null));
        Callable callable = (Callable) cl.loadClass(TestCase.class.getName()).newInstance();
        Assertions.assertThrows(NoClassDefFoundError.class, () -> callable.call());
    }

    @Test
    public void testObjenesis() throws Throwable {
        new ObjenesisAllocator().newInstance(Integer.class);
    }

}
