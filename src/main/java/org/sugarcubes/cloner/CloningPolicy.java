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

/**
 * Set of rules for objects cloning.
 *
 * @author Maxim Butov
 */
public interface CloningPolicy {

    /**
     * Default immutable policy suitable for most cases.
     */
    CloningPolicy DEFAULT = new CloningPolicy() {
    };

    /**
     * JDK immutable types.
     */
    Set<Class<?>> IMMUTABLE_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
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
     * Primitive wrappers types.
     */
    Set<Class<?>> WRAPPERS_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        Boolean.class, Byte.class, Character.class, Short.class, Integer.class, Long.class, Float.class, Double.class
    )));

    /**
     * Checks the type is primitive wrapper.
     *
     * @param type type
     * @return {@code true} if {@code type} is primitive wrapper
     */
    static boolean isWrapper(Class<?> type) {
        return WRAPPERS_TYPES.contains(type);
    }

    /**
     * Checks the type is primitive or enum or primitive wrapper.
     *
     * @param type type
     * @return {@code true} if {@code type} is primitive or enum or primitive wrapper
     */
    static boolean isPrimitiveOrEnumOrWrapper(Class<?> type) {
        return type.isPrimitive() || type.isEnum() || isWrapper(type);
    }

    /**
     * Returns action to apply to a field value.
     *
     * @param field field
     * @return action
     */
    default CopyAction getFieldAction(Field field) {
        return isComponentTypeImmutable(field.getType()) ? CopyAction.ORIGINAL : CopyAction.DEFAULT;
    }

    /**
     * Returns action to apply to an instance of the type.
     *
     * @param type type
     * @return action
     */
    default CopyAction getTypeAction(Class<?> type) {
        return isImmutable(type) ? CopyAction.ORIGINAL : CopyAction.DEFAULT;
    }

    /**
     * Returns {@code true} if objects of the type are immutable and may be copied by reference.
     *
     * @param type type
     * @return {@code true} if objects of the type are immutable
     */
    default boolean isImmutable(Class<?> type) {
        return IMMUTABLE_TYPES.contains(type);
    }

    /**
     * Returns {@code true} if type is immutable and final. This means there is no mutable subtype of this type.
     *
     * @param type type
     * @return {@code true} if type is immutable and final
     */
    default boolean isFinalAndImmutable(Class<?> type) {
        return Modifier.isFinal(type.getModifiers()) && isImmutable(type);
    }

    /**
     * Returns {@code true} if array component type or field type guarantees that the stored value is immutable.
     * We cannot use {@code isImmutable(componentType)}, because, for instance, the value of field defined as
     * {@code private BigInteger value;} will be immutable value in this case, although it can refer to some mutable subtype of
     * {@code BigInteger}.
     *
     * @param componentType array component type of field type
     * @return {@code true} if value guaranteed to be immutable
     */
    default boolean isComponentTypeImmutable(Class<?> componentType) {
        return CloningPolicy.isPrimitiveOrEnumOrWrapper(componentType) || isFinalAndImmutable(componentType);
    }

}
