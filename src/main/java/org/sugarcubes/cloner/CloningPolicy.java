package org.sugarcubes.cloner;

import java.lang.reflect.Field;
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
     * Returns action to apply to a field value.
     *
     * @param field field
     * @return action
     */
    default CopyAction getFieldAction(Field field) {
        return CloningPolicyHelper.isComponentTypeImmutable(this, field.getType()) ? CopyAction.ORIGINAL : CopyAction.DEFAULT;
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
