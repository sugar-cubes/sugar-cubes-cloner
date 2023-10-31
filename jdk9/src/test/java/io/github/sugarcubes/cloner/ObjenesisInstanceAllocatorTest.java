/*
 * Copyright 2017-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.sugarcubes.cloner;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.sugarcubes.cloner.internal.ObjenesisInstanceAllocatorFactory;

public class ObjenesisInstanceAllocatorTest {

    public static class TestCase implements Callable {

        @Override
        public Object call() throws Exception {
            return new ObjenesisInstanceAllocatorFactory();
        }

    }

    @Test
    public void testNoObjenesis() throws Throwable {

        CustomClassLoader cl = new CustomClassLoader().disable("org.objenesis.").reload("io.github.sugarcubes.");
        Class<?> objenesisUtilsClass = cl.loadClass(ObjenesisUtils.class.getName());
        Method isObjenesisAvailableMethod = objenesisUtilsClass.getDeclaredMethod("isObjenesisAvailable");

        Assertions.assertEquals(false, isObjenesisAvailableMethod.invoke(null));
        Callable callable = (Callable) cl.loadClass(TestCase.class.getName()).newInstance();
        Assertions.assertThrows(NoClassDefFoundError.class, () -> callable.call());
    }

    @Test
    public void testObjenesis() throws Throwable {
        new ObjenesisInstanceAllocatorFactory().newAllocator(Integer.class).newInstance();
    }

}
