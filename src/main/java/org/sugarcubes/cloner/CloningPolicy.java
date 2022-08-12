package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.sugarcubes.cloner.CloningPolicyHelper.isComponentTypeImmutable;

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
    )));

    /**
     * Returns action to apply to a field value. This action is not applied to primitive fields, they are always copied by value.
     *
     * @param field field
     * @return action
     */
    default CopyAction getFieldAction(Field field) {
        return isComponentTypeImmutable(this, field.getType()) ? CopyAction.ORIGINAL : CopyAction.DEFAULT;
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

}
