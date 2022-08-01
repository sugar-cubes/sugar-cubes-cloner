package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public interface CloningPolicy {

    /**
     * JDK immutable classes.
     */
    Set<Class<?>> IMMUTABLE_CLASSES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        BigDecimal.class, BigInteger.class, Boolean.class, Byte.class,
        Character.class, Class.class,
        Double.class, Duration.class,
        Float.class,
        Instant.class, Integer.class,
        LocalDate.class, LocalDateTime.class, LocalTime.class, Long.class,
        MonthDay.class,
        OffsetDateTime.class, OffsetTime.class,
        Pattern.class, Period.class,
        Short.class, String.class,
        URI.class, URL.class, UUID.class,
        Year.class, YearMonth.class,
        ZonedDateTime.class, ZoneOffset.class
    )));

    /**
     * Primitive wrappers classes.
     */
    Set<Class<?>> WRAPPERS_CLASSES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        Boolean.class, Byte.class, Character.class, Short.class, Integer.class, Long.class, Float.class, Double.class
    )));

    static boolean isWrapper(Class<?> type) {
        return WRAPPERS_CLASSES.contains(type);
    }

    static boolean isPrimitiveOrEnumOrWrapper(Class<?> type) {
        return type.isPrimitive() || type.isEnum() || isWrapper(type);
    }

    default CloningAction getFieldAction(Field field) {
        return isComponentTypeImmutable(field.getType()) ? CloningAction.ORIGINAL : CloningAction.DEFAULT;
    }

    default CloningAction getTypeAction(Class<?> type) {
        return isImmutable(type) ? CloningAction.ORIGINAL : CloningAction.DEFAULT;
    }

    default boolean isImmutable(Class<?> type) {
        return IMMUTABLE_CLASSES.contains(type);
    }

    default boolean isFinalAndImmutable(Class<?> type) {
        return Modifier.isFinal(type.getModifiers()) && isImmutable(type);
    }

    default boolean isComponentTypeImmutable(Class<?> componentType) {
        return CloningPolicy.isPrimitiveOrEnumOrWrapper(componentType) || isFinalAndImmutable(componentType);
    }


}
