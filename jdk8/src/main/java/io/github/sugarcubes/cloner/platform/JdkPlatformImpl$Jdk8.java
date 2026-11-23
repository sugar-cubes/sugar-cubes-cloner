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
package io.github.sugarcubes.cloner.platform;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Supplier;

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.ModuleOpening;
import io.github.sugarcubes.cloner.generation.BytecodeBackend;
import io.github.sugarcubes.cloner.reflection.ReflectionImpl$Jdk8;
import io.github.sugarcubes.cloner.spi.ContainerModel;
import io.github.sugarcubes.cloner.spi.FieldAdapterFactory;
import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.spi.ValuesModel;
import io.github.sugarcubes.cloner.unsafe.SunMiscUnsafeBridge;
import io.github.sugarcubes.cloner.unsafe.UnsafeBridge;
import io.github.sugarcubes.cloner.util.ClassUtils;
import io.github.sugarcubes.cloner.util.ClonerExceptionUtils;
import io.github.sugarcubes.cloner.util.Lazy;

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
    private static final Set<Class<?>> IMMUTABLE_TYPES = unmodifiableSet(
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
        // emptyNavigableMap() and emptySortedMap(), the only instance of a private class, same for the sets
        Collections.emptyNavigableMap().getClass(), Collections.emptyNavigableSet().getClass(),
        Spliterators.emptySpliterator().getClass(), Spliterators.emptyIntSpliterator().getClass(),
        Spliterators.emptyLongSpliterator().getClass(), Spliterators.emptyDoubleSpliterator().getClass(),
        String.CASE_INSENSITIVE_ORDER.getClass()
    );

    /**
     * Immutable types with all their subtypes: canonical or immutable instances of not public classes.
     */
    private static final List<Class<?>> IMMUTABLE_SUPER_TYPES = unmodifiableList(
        java.nio.charset.Charset.class,
        java.nio.file.Path.class,
        java.time.ZoneId.class,
        java.time.chrono.Chronology.class
    );

    /**
     * Types copied with their public {@code clone()} method. Exact classes.
     */
    private static final Set<Class<?>> CLONEABLE_TYPES = unmodifiableSet(
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
    );

    /**
     * Objects of the running JVM with all their subtypes: threads, class loaders, reflection objects, references.
     */
    private static final List<Class<?>> RUNTIME_SUPER_TYPES = unmodifiableList(
        Thread.class, ThreadGroup.class, ClassLoader.class, Runtime.class,
        java.lang.reflect.AccessibleObject.class, java.lang.reflect.Parameter.class,
        java.lang.invoke.MethodHandle.class, java.lang.invoke.MethodType.class,
        java.lang.invoke.MethodHandles.Lookup.class,
        java.lang.ref.Reference.class, java.lang.ref.ReferenceQueue.class
    );

    private static final ContainerModels CONTAINER_MODELS = new ContainerModels();

    /**
     * Version of anonymous classes: Java 5, verified by type inference without stack map frames. Frames would name
     * the class itself, and an anonymous class cannot be loaded by name.
     */
    private static final int ANONYMOUS_CLASS_VERSION = 49;

    /**
     * {@code sun.misc.Unsafe.defineAnonymousClass(Class, byte[], Object[])}, removed in JDK 17.
     */
    private static final Lazy<Optional<Method>> DEFINE_ANONYMOUS_CLASS = new Lazy<>(() -> {
        try {
            return Optional.of(sun.misc.Unsafe.class.getMethod("defineAnonymousClass", Class.class, byte[].class,
                Object[].class));
        }
        catch (NoSuchMethodException | LinkageError e) {
            return Optional.empty();
        }
    });

    /**
     * Kinds of classes, computed once by {@link #kindOf(Class)}.
     */
    private final ClassValue<Kind> kinds = new ClassValue<Kind>() {
        @Override
        protected Kind computeValue(Class<?> type) {
            return kindOf(type);
        }
    };

    /**
     * Determines the kind of the class. A newer JDK version overrides it for its own types and delegates the others
     * to the older version.
     *
     * @param type class
     * @return kind of the class
     */
    Kind kindOf(Class<?> type) {
        if (IMMUTABLE_TYPES.contains(type) || isSubtype(type, IMMUTABLE_SUPER_TYPES)) {
            return Kind.IMMUTABLE;
        }
        if (CLONEABLE_TYPES.contains(type)) {
            return Kind.CLONEABLE;
        }
        if (isSubtype(type, RUNTIME_SUPER_TYPES)) {
            return Kind.RUNTIME;
        }
        return Kind.OTHER;
    }

    /**
     * Tells whether the class is a subtype of one of the types.
     *
     * @param type class
     * @param superTypes types
     * @return {@code true} if the class is a subtype of one of the types
     */
    static boolean isSubtype(Class<?> type, List<Class<?>> superTypes) {
        return superTypes.stream().anyMatch(superType -> superType.isAssignableFrom(type));
    }

    /**
     * Creates an unmodifiable list.
     *
     * @param types types
     * @return unmodifiable list
     */
    static List<Class<?>> unmodifiableList(Class<?>... types) {
        return Collections.unmodifiableList(Arrays.asList(types));
    }

    private static Set<Class<?>> unmodifiableSet(Class<?>... types) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(types)));
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
    public Reflection reflection(ModuleOpening opening, Supplier<UnsafeBridge> unsafe) {
        // no modules, nothing to open
        return new ReflectionImpl$Jdk8();
    }

    @Override
    public boolean canDefineNestmates() {
        return DEFINE_ANONYMOUS_CLASS.get().isPresent();
    }

    @Override
    public int nestmateClassVersion() {
        return ANONYMOUS_CLASS_VERSION;
    }

    /**
     * Defines an anonymous class of {@code sun.misc.Unsafe}, which has the access of its host class; JDK 8-16.
     */
    @Override
    public MethodHandle defineNestmate(Class<?> host, byte[] bytes, Class<?> support, MethodType constructor) {
        Method define = DEFINE_ANONYMOUS_CLASS.get()
            .orElseThrow(() -> new ClonerException("Unsafe.defineAnonymousClass() is not available."));
        return ClonerExceptionUtils.replaceException(() -> {
            Object unsafe = new SunMiscUnsafeBridge(reflection()).getUnsafe();
            Class<?> anonymous = (Class<?>) define.invoke(unsafe, host, bytes, null);
            // not public: its constructor is made accessible
            Constructor<?> declared = anonymous.getDeclaredConstructor(constructor.parameterArray());
            declared.setAccessible(true);
            return MethodHandles.lookup().unreflectConstructor(declared);
        });
    }

    @Override
    public Optional<BytecodeBackend> classFileBackend() {
        return Optional.empty();
    }

    @Override
    public MethodHandles.Lookup trustedLookup() {
        return TrustedLookup.byReflection();
    }

    @Override
    public Optional<FieldAdapterFactory> varHandleFieldAdapters() {
        return Optional.empty();
    }

    @Override
    public <T> Optional<ContainerModel<T>> containerModel(Class<T> type) {
        return CONTAINER_MODELS.containerModel(type);
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
