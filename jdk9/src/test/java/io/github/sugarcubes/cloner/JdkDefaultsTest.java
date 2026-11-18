/*
 * Copyright 2017-2024 the original author or authors.
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Default copying of JDK types: immutable objects and objects of the running JVM are shared, some types are cloned.
 */
class JdkDefaultsTest {

    static class Holder {

        Locale locale = Locale.GERMANY;
        Currency currency = Currency.getInstance("EUR");
        Charset charset = StandardCharsets.UTF_8;
        Path path = Paths.get("/tmp");
        ZoneId zone = ZoneId.of("Europe/Berlin");
        List<Object> emptyList = Collections.emptyList();
        Comparator<String> comparator = String.CASE_INSENSITIVE_ORDER;
        Thread thread = Thread.currentThread();
        ClassLoader classLoader = getClass().getClassLoader();
        Module module = getClass().getModule();
        Method method;
        Date date = new Date(42);
        BitSet bits = new BitSet();

        Holder() throws NoSuchMethodException {
            method = Object.class.getMethod("toString");
            bits.set(3);
        }

    }

    @Test
    void sharedAndClonedJdkObjects() throws Exception {
        Holder original = new Holder();

        Holder clone = Cloners.reflection().clone(original);

        Assertions.assertNotSame(original, clone);
        Assertions.assertSame(original.locale, clone.locale);
        Assertions.assertSame(original.currency, clone.currency);
        Assertions.assertSame(original.charset, clone.charset);
        Assertions.assertSame(original.path, clone.path);
        Assertions.assertSame(original.zone, clone.zone);
        Assertions.assertSame(original.emptyList, clone.emptyList);
        Assertions.assertSame(original.comparator, clone.comparator);
        Assertions.assertSame(original.thread, clone.thread);
        Assertions.assertSame(original.classLoader, clone.classLoader);
        Assertions.assertSame(original.module, clone.module);
        Assertions.assertSame(original.method, clone.method);
        Assertions.assertNotSame(original.date, clone.date);
        Assertions.assertEquals(original.date, clone.date);
        Assertions.assertNotSame(original.bits, clone.bits);
        Assertions.assertEquals(original.bits, clone.bits);
    }

    @Test
    void customRulesOverrideDefaults() throws Exception {
        Date custom = new Date(1);
        Cloner cloner = Cloners.builder()
            .typeAction(Locale.class, CopyAction.NULL)
            .copier(Date.class, (original, context) -> context.register(custom))
            .build();

        Holder clone = cloner.clone(new Holder());

        Assertions.assertNull(clone.locale);
        Assertions.assertSame(custom, clone.date);
    }

}
