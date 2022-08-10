package org.sugarcubes.cloner;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@SuppressWarnings("checkstyle:all")
public class TestObjectFactory {

    /**
     * Three-arguments consumer.
     */
    interface TernaryConsumer<T, U, V> {

        void accept(T t, U u, V v);

    }

    private static final Random random = new SecureRandom();

    public static Object randomObject(int width, int depth) {
        if (depth == 0) {
            return randomSupply(
                random::nextInt,
                random::nextLong,
                random::nextDouble,
                TestObjectFactory::randomString,
                TestObjectFactory::randomInstant,
                TestObjectFactory::randomLocalDateTime,
                () -> randomPrimitiveArray(random(boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, double.class), 1 + random.nextInt(width))
            );
        }
        Object[] array = randomObjectArray(width, depth);
        return randomSupply(
            () -> array,
            () -> {
                Collection<Object> collection = newCollection();
                collection.addAll(Arrays.asList(array));
                return collection;
            },
            () -> {
                Map<String, Object> map = newMap();
                for (Object obj : array) {
                    map.put(randomString(), obj);
                }
                return map;
            }
        );
    }

    private static String randomString() {
        return new BigInteger(256, random).toString(36);
    }

    private static Date randomDate() {
        return new Date(System.currentTimeMillis() + random.nextInt());
    }

    private static GregorianCalendar randomCalendar() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(randomDate());
        return calendar;
    }

    private static Instant randomInstant() {
        return randomDate().toInstant();
    }

    private static LocalDateTime randomLocalDateTime() {
        return LocalDateTime.ofInstant(randomInstant(), ZoneId.of(random(ZoneOffset.getAvailableZoneIds())));
    }

    @SafeVarargs
    private static <T> T random(T... array) {
        return array[random.nextInt(array.length)];
    }

    private static <T> T random(Collection<T> collection) {
        return (T) random(collection.toArray());
    }

    private static <T> T randomSupply(Supplier<T>... suppliers) {
        return random(suppliers).get();
    }

    private static Object randomPrimitiveArray(Class<?> componentType, int length) {
        Object array = Array.newInstance(componentType, length);
        if (componentType == boolean.class) {
            return fillArray(array, Array::setBoolean, random::nextBoolean);
        }
        if (componentType == byte.class) {
            return fillArray(array, Array::setByte, () -> (byte) random.nextInt());
        }
        if (componentType == char.class) {
            return fillArray(array, Array::setChar, () -> (char) random.nextInt());
        }
        if (componentType == short.class) {
            return fillArray(array, Array::setShort, () -> (short) random.nextInt());
        }
        if (componentType == int.class) {
            return fillArray(array, Array::setInt, random::nextInt);
        }
        if (componentType == long.class) {
            return fillArray(array, Array::setLong, random::nextLong);
        }
        if (componentType == float.class) {
            return fillArray(array, Array::setFloat, random::nextFloat);
        }
        if (componentType == double.class) {
            return fillArray(array, Array::setDouble, random::nextDouble);
        }
        throw new IllegalStateException();
    }

    private static Object[] randomObjectArray(int width, int depth) {
        Object[] array = new Object[width];
        for (int k = 0; k < array.length; k++) {
            array[k] = randomObject(Math.max(width + random.nextInt(3) - 1, 1), random.nextInt(depth));
        }
        return array;
    }

    private static <X> Object fillArray(Object array, TernaryConsumer<Object, Integer, X> setter, Supplier<X> elementSupplier) {
        int length = Array.getLength(array);
        for (int k = 0; k < length; k++) {
            setter.accept(array, k, elementSupplier.get());
        }
        return array;
    }

    private static <T> T newInstance(Class<?>... types) {
        try {
            Class<?> type = random(types);
            return (T) type.getDeclaredConstructor().newInstance();
        }
        catch (Error | RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <K, V> Map<K, V> newMap() {
        return newInstance(ConcurrentHashMap.class, HashMap.class, Hashtable.class, IdentityHashMap.class, LinkedHashMap.class, TreeMap.class);
    }

    private static <T> Collection<T> newCollection() {
        return newInstance(ArrayDeque.class, ArrayList.class, HashSet.class, LinkedHashSet.class, LinkedList.class, Stack.class, Vector.class);
    }

}
