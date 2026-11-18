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
package io.github.sugarcubes.cloner.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterators;

import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.spi.TypeOpener;
import io.github.sugarcubes.cloner.spi.ValuesModel;
import io.github.sugarcubes.cloner.util.ClassUtils;

/**
 * {@link JdkPlatform} of JDK 8.
 *
 * @author Maxim Butov
 */
class JdkPlatformImpl$Jdk8 implements JdkPlatform {

    /**
     * Kinds of JDK types.
     */
    enum Kind {
        IMMUTABLE,
        CLONEABLE,
        RUNTIME,
        OTHER,
    }

    /**
     * Immutable types: values, canonical instances, classes of stateless singletons. Exact classes.
     */
    protected final Set<Class<?>> immutableTypes = new HashSet<>(Arrays.asList(
        java.math.BigDecimal.class, java.math.BigInteger.class, java.math.MathContext.class,
        Boolean.class, Byte.class, Character.class, Class.class, Double.class, Float.class, Integer.class, Long.class,
        Short.class, String.class,
        java.io.File.class,
        java.net.Inet4Address.class, java.net.Inet6Address.class, java.net.InetSocketAddress.class,
        java.net.NetworkInterface.class, java.net.URI.class, java.net.URL.class,
        java.time.Duration.class, java.time.Instant.class, java.time.LocalDate.class, java.time.LocalDateTime.class,
        java.time.LocalTime.class, java.time.MonthDay.class, java.time.OffsetDateTime.class,
        java.time.OffsetTime.class, java.time.Period.class, java.time.Year.class, java.time.YearMonth.class,
        java.time.ZonedDateTime.class, java.time.format.DateTimeFormatter.class,
        java.util.Currency.class, java.util.Locale.class, java.util.UUID.class, java.util.regex.Pattern.class,
        Collections.emptyEnumeration().getClass(), Collections.emptyIterator().getClass(),
        Collections.emptyList().getClass(), Collections.emptyListIterator().getClass(),
        Collections.emptyMap().getClass(), Collections.emptySet().getClass(),
        Spliterators.emptySpliterator().getClass(), Spliterators.emptyIntSpliterator().getClass(),
        Spliterators.emptyLongSpliterator().getClass(), Spliterators.emptyDoubleSpliterator().getClass(),
        String.CASE_INSENSITIVE_ORDER.getClass()
    ));

    /**
     * Immutable types with all their subtypes: canonical or immutable instances of not public classes.
     */
    protected final List<Class<?>> immutableSuperTypes = new ArrayList<>(Arrays.asList(
        java.nio.charset.Charset.class,
        java.nio.file.Path.class,
        java.time.ZoneId.class,
        java.time.chrono.Chronology.class
    ));

    /**
     * Types copied with their public {@code clone()} method. Exact classes.
     */
    protected final Set<Class<?>> cloneableTypes = new HashSet<>(Arrays.asList(
        java.util.BitSet.class,
        java.util.Date.class,
        java.util.GregorianCalendar.class,
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
     * Objects of the running JVM with all their subtypes: threads, class loaders, reflection objects, references.
     */
    protected final List<Class<?>> runtimeSuperTypes = new ArrayList<>(Arrays.asList(
        Thread.class, ThreadGroup.class, ClassLoader.class, Runtime.class,
        java.lang.reflect.AccessibleObject.class, java.lang.reflect.Parameter.class,
        java.lang.invoke.MethodHandle.class, java.lang.invoke.MethodType.class,
        java.lang.invoke.MethodHandles.Lookup.class,
        java.lang.ref.Reference.class, java.lang.ref.ReferenceQueue.class
    ));

    /**
     * Singleton instances whose classes also have other instances (unmodifiable wrappers).
     */
    protected final Set<Object> systemWideSingletons = Collections.newSetFromMap(new IdentityHashMap<>());

    private final ClassValue<Kind> kinds = new ClassValue<Kind>() {
        @Override
        protected Kind computeValue(Class<?> type) {
            return kindOf(type);
        }
    };

    JdkPlatformImpl$Jdk8() {
        systemWideSingletons.addAll(Arrays.asList(
            Collections.emptyNavigableMap(),
            Collections.emptyNavigableSet(),
            Collections.emptySortedMap(),
            Collections.emptySortedSet()
        ));
    }

    private Kind kindOf(Class<?> type) {
        if (immutableTypes.contains(type) || isSubtype(type, immutableSuperTypes)) {
            return Kind.IMMUTABLE;
        }
        if (cloneableTypes.contains(type)) {
            return Kind.CLONEABLE;
        }
        if (isSubtype(type, runtimeSuperTypes)) {
            return Kind.RUNTIME;
        }
        return Kind.OTHER;
    }

    private static boolean isSubtype(Class<?> type, List<Class<?>> superTypes) {
        return superTypes.stream().anyMatch(superType -> superType.isAssignableFrom(type));
    }

    @Override
    public boolean isImmutable(Class<?> type) {
        return kinds.get(type) == Kind.IMMUTABLE;
    }

    @Override
    public boolean isCloneable(Class<?> type) {
        return kinds.get(type) == Kind.CLONEABLE;
    }

    @Override
    public boolean isRuntime(Class<?> type) {
        return kinds.get(type) == Kind.RUNTIME;
    }

    @Override
    public Set<Object> getSystemWideSingletons() {
        return Collections.unmodifiableSet(systemWideSingletons);
    }

    @Override
    public Reflection reflection(TypeOpener opener) {
        // no modules, nothing to open
        return new ReflectionImpl$Jdk8();
    }

    @Override
    public UnsafeBridge unsafeBridge(Reflection reflection) {
        return new SunMiscUnsafeBridge(reflection);
    }

    @Override
    public boolean isHidden(Class<?> type) {
        // lambda classes are synthetic VM anonymous classes
        return type.isSynthetic() && type.getName().contains("$$Lambda");
    }

    @Override
    public <T> Optional<ValuesModel<T>> valuesModel(Class<T> type, Reflection reflection) {
        return Optional.empty();
    }

}
