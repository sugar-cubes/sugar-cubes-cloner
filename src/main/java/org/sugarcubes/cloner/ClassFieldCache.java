package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Cache (type => list of non-static fields).
 *
 * @author Maxim Butov
 */
public class ClassFieldCache {

    /**
     * Actual cache.
     */
    private final Map<Class<?>, List<Field>> cache = new HashMap<>();

    /**
     * Constructor.
     */
    public ClassFieldCache() {
        this.cache.put(null, Collections.emptyList());
    }

    /**
     * Returns list of non-static fields of type.
     */
    public List<Field> get(Class<?> type) {
        List<Field> fields = cache.get(type);
        if (fields == null) {
            fields = Collections.unmodifiableList(
                Stream.concat(
                        get(type.getSuperclass()).stream(),
                        Arrays.stream(type.getDeclaredFields())
                            .filter(field -> !Modifier.isStatic(field.getModifiers()))
                            .peek(ReflectionUtils::makeAccessible)
                    )
                    .collect(Collectors.toList())
            );
            cache.put(type, fields);
        }
        return fields;
    }

}
