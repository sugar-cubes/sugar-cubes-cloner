/*
 * Copyright 2017-2026 the original author or authors.
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
package io.github.sugarcubes.cloner.fields;

import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.sugarcubes.cloner.platform.JdkPlatform;
import io.github.sugarcubes.cloner.spi.FieldAdapter;
import io.github.sugarcubes.cloner.spi.FieldAdapterFactory;
import io.github.sugarcubes.cloner.spi.Reflection;

/**
 * JDK 16+ denies illegal reflective access by default; java.util.regex is not opened to the test JVM.
 */
class CompoundFieldAdapterFactoryTest {

    private final Reflection reflection = JdkPlatform.current().lowLevelAccess().reflection();

    private final FieldAdapterFactory reflective = ReflectionFieldAdapterImpl.factory(reflection);

    private final FieldAdapterFactory unsafe =
        UnsafeFieldAdapterImpl.factory(JdkPlatform.current().lowLevelAccess().unsafeBridge());

    @Test
    void reflectionCannotAccessFieldOfNotOpenedPackage() throws Exception {
        Assertions.assertTrue(reflective.newAdapter(Pattern.class.getDeclaredField("pattern")).isEmpty());
    }

    @Test
    void unsafeIsUsedWhenReflectionCannotAccess() throws Exception {
        Field field = Pattern.class.getDeclaredField("pattern");
        FieldAdapterFactory compound = new CompoundFieldAdapterFactory(List.of(reflective, unsafe));

        FieldAdapter adapter = compound.newAdapter(field).orElseThrow();

        Assertions.assertInstanceOf(UnsafeFieldAdapterImpl.class, adapter);
        Assertions.assertEquals("a+", adapter.getObject(Pattern.compile("a+")));
    }

    @Test
    void reflectionIsPreferredWhenItCanAccess() throws Exception {
        Field field = Sample.class.getDeclaredField("value");
        FieldAdapterFactory compound = new CompoundFieldAdapterFactory(List.of(reflective, unsafe));

        Assertions.assertInstanceOf(ReflectionFieldAdapterImpl.class, compound.newAdapter(field).orElseThrow());
    }

    static class Sample {

        private int value;

    }

}
