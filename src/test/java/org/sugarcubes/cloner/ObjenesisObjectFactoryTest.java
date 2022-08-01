package org.sugarcubes.cloner;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Maxim Butov
 */
public class ObjenesisObjectFactoryTest {

    public static class TestCase implements Callable {

        @Override
        public Object call() throws Exception {
            return new ObjenesisObjectAllocator();
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
        new ObjenesisObjectAllocator().newInstance(Integer.class);
    }

}
