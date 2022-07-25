package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link Cloner} which copies objects fields using Reflection API.
 *
 * @author Maxim Butov
 */
public class ReflectionCloner extends AbstractCloner {

    /**
     * Object factory.
     */
    private final ClonerObjectFactory objectFactory;

    /**
     * Default constructor. Uses {@link ObjenesisObjectFactory} if Objenesis library is present, or
     * {@link ReflectionObjectFactory} otherwise.
     */
    public ReflectionCloner() {
        this(ObjenesisUtils.isObjenesisAvailable() ? new ObjenesisObjectFactory() : new ReflectionObjectFactory());
    }

    /**
     * Creates an instance with the specified object factory.
     *
     * @param objectFactory object factory to use
     */
    public ReflectionCloner(ClonerObjectFactory objectFactory) {
        this.objectFactory = Objects.requireNonNull(objectFactory, "objectFactory required");
    }

    /**
     * Cloner's object factory.
     *
     * @return the object factory
     */
    public ClonerObjectFactory getObjectFactory() {
        return objectFactory;
    }

    @Override
    protected Object doClone(Object object) throws Throwable {
        return doClone(object, new IdentityHashMap<>());
    }

    /**
     * Deep-clones the object.
     *
     * @param object object to clone
     * @param cloned map of previously cloned objects, needed it to avoid repeatable cloning and to break cycles in object graph
     *
     * @return clone
     */
    protected Object doClone(Object object, Map<Object, Object> cloned) throws Throwable {
        if (object == null) {
            return null;
        }
        Class<?> type = object.getClass();
        if (type.isEnum() || isImmutable(type)) {
            return object;
        }
        Object clone = cloned.get(object);
        return clone != null ? clone : doCloneObjectOrArray(object, cloned);
    }

    protected Object doCloneObjectOrArray(Object object, Map<Object, Object> cloned) throws Throwable {
        return object.getClass().isArray() ? doCloneArray(object, cloned) : doCloneObject(object, cloned);
    }

    private static final Method CLONE_METHOD = ReflectionUtils.getMethod(Object.class, "clone");

    /**
     * Clones the array.
     *
     * @param array array
     * @param cloned map of previously cloned objects
     * @return clone
     */
    protected Object doCloneArray(Object array, Map<Object, Object> cloned) throws Throwable {
        Object clone = CLONE_METHOD.invoke(array);
        cloned.put(array, clone);

        Class<?> componentType = array.getClass().getComponentType();
        if (!componentType.isPrimitive() && !componentType.isEnum()) {
            Object[] source = (Object[]) array;
            Object[] target = (Object[]) clone;
            int length = source.length;
            for (int k = 0; k < length; k++) {
                target[k] = doClone(source[k], cloned);
            }
        }

        return clone;
    }

    /**
     * Some immutable classes.
     */
    private static final Set<Class<?>> DEFAULT_IMMUTABLE_CLASSES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
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

    protected boolean isImmutable(Class<?> type) {
        return type.isEnum() || DEFAULT_IMMUTABLE_CLASSES.contains(type);
    }

    /**
     * Clones non-array object.
     *
     * @param object object
     * @param cloned map of previously cloned objects
     * @return clone
     */
    protected Object doCloneObject(Object object, Map<Object, Object> cloned) throws Throwable {
        Object clone = getObjectFactory().newInstance(object.getClass());
        cloned.put(object, clone);
        copyFields(object, clone, cloned);
        return clone;
    }

    protected void copyFields(Object from, Object into, Map<Object, Object> cloned) throws Throwable {
        for (Field field : getCopyableFields(from.getClass())) {
            copyField(from, into, field, cloned);
        }
    }

    protected void copyField(Object from, Object into, Field field, Map<Object, Object> cloned) throws Throwable {
        field.set(into, doClone(field.get(from), cloned));
    }

    /**
     * Cache with (class, fields to copy) entries.
     */
    private final Map<Class<?>, Collection<Field>> copyableFieldsCache = new HashMap<>();

    protected Collection<Field> getCopyableFields(Class<?> clazz) {
        return clazz != null ? copyableFieldsCache.computeIfAbsent(clazz, this::findCopyableFields) : Collections.emptyList();
    }

    protected List<Field> findCopyableFields(Class<?> clazz) {
        return Stream.concat(
            Stream.of(clazz.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .peek(ReflectionUtils::makeAccessible)
                .peek(ReflectionUtils::clearFinalModifier),
            getCopyableFields(clazz.getSuperclass()).stream()
        ).collect(Collectors.toList());
    }

}
