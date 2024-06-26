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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.Spliterators;

/**
 * Implementation of {@link JdkConfiguration} for JDK 8+.
 *
 * @author Maxim Butov
 */
class JdkConfigurationImpl$Jdk8 implements JdkConfiguration {

    /**
     * JDK immutable types.
     */
    protected final Set<Class<?>> immutableTypes = new HashSet<>(Arrays.asList(
        java.math.BigDecimal.class, java.math.BigInteger.class, Boolean.class, Byte.class,
        Character.class, Class.class,
        Double.class, java.time.Duration.class,
        Float.class,
        java.net.Inet4Address.class, java.net.Inet6Address.class, java.net.InetSocketAddress.class, java.time.Instant.class,
        Integer.class,
        java.time.LocalDate.class, java.time.LocalDateTime.class, java.time.LocalTime.class, Long.class,
        java.time.MonthDay.class,
        java.net.NetworkInterface.class,
        java.time.OffsetDateTime.class, java.time.OffsetTime.class,
        java.util.regex.Pattern.class, java.time.Period.class,
        Short.class, String.class,
        java.net.URI.class, java.net.URL.class, java.util.UUID.class,
        java.time.Year.class, java.time.YearMonth.class,
        java.time.ZonedDateTime.class, java.time.ZoneOffset.class
    ));

    /**
     * JDK cloneable types.
     */
    protected final Set<Class<?>> cloneableTypes = new HashSet<>(Arrays.asList(
        java.util.BitSet.class,
        java.util.Date.class,
        java.util.GregorianCalendar.class,
        java.util.Locale.class,
        ClassUtils.classForName("java.util.JumboEnumSet"),
        ClassUtils.classForName("java.util.RegularEnumSet"),
        java.text.ChoiceFormat.class,
        java.text.DateFormat.class,
        java.text.DateFormatSymbols.class,
        java.text.DecimalFormat.class,
        java.text.DecimalFormatSymbols.class,
        ClassUtils.classForName("java.text.DigitList"),
        java.text.MessageFormat.class,
        java.text.SimpleDateFormat.class
    ));

    /**
     * System wide singletons.
     */
    protected final Set<Object> systemWideSingletons = Collections.newSetFromMap(new IdentityHashMap<>());

    private UnsafeBridge unsafe;

    public JdkConfigurationImpl$Jdk8() {
        systemWideSingletons.addAll(Arrays.asList(
            Collections.emptyEnumeration(),
            Collections.emptyIterator(),
            Collections.emptyList(),
            Collections.emptyListIterator(),
            Collections.emptyMap(),
            Collections.emptyNavigableMap(),
            Collections.emptyNavigableSet(),
            Collections.emptySet(),
            Collections.emptySortedMap(),
            Collections.emptySortedSet(),
            Spliterators.emptyDoubleSpliterator(),
            Spliterators.emptyIntSpliterator(),
            Spliterators.emptyLongSpliterator(),
            Spliterators.emptySpliterator()
        ));
    }

    @Override
    public Set<Class<?>> getImmutableTypes() {
        return Collections.unmodifiableSet(immutableTypes);
    }

    @Override
    public Set<Class<?>> getCloneableTypes() {
        return Collections.unmodifiableSet(cloneableTypes);
    }

    @Override
    public Set<Object> getSystemWideSingletons() {
        return Collections.unmodifiableSet(systemWideSingletons);
    }

    @Override
    public ObjectCopier<?> getCopier(Class<?> type) {
        return null;
    }

    @Override
    public UnsafeBridge getUnsafe() {
        return unsafe != null ? unsafe : (unsafe = getUnsafeImpl());
    }

    protected UnsafeBridge getUnsafeImpl() {
        return new SunMiscUnsafeBridge();
    }

    @Override
    public void makeAccessible(Class<?> type) {
        // good old Java 8, nothing to do
    }

}
