package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cache (type, list of non-static fields).
 *
 * @author Maxim Butov
 */
public final class ClassFieldCache {

    /**
     * Actual cache.
     */
    private final Map<Class<?>, List<Field>> cache = new HashMap<>();

    /**
     * Constructor.
     */
    public ClassFieldCache() {
    }

    /**
     * Returns list of non-static fields of type.
     *
     * @param type type
     * @return list of non-static fields
     */
    public List<Field> get(Class<?> type) {
        List<Field> fields = cache.get(type);
        if (fields != null) {
            return fields;
        }
        Class<?> superType = type.getSuperclass();
        List<Field> superFields = superType != null ? get(superType) : Collections.emptyList();
        return cache.computeIfAbsent(type, t -> findFields(type, superFields));
    }

    private List<Field> findFields(Class<?> type, List<Field> superFields) {
        List<Field> fields = Arrays.stream(type.getDeclaredFields())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .peek(ReflectionUtils::makeAccessible)
            .collect(Collectors.toList());
        if (fields.isEmpty()) {
            return superFields;
        }
        fields.addAll(superFields);
        return fields;
    }

}
