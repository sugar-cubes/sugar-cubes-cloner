package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Field copiers factory which uses reflection for getting/setting field values.
 *
 * @author Maxim Butov
 */
public class ReflectionFieldCopierFactory implements FieldCopierFactory {

    /**
     * Primitive field copiers factories.
     */
    private static final Map<Class<?>, Function<Field, FieldCopier>> PRIMITIVE_FIELD_COPIERS;

    static {
        Map<Class<?>, Function<Field, FieldCopier>> primitiveFieldCopiers = new HashMap<>();

        primitiveFieldCopiers.put(boolean.class,
            field -> ((original, clone, context) -> field.setBoolean(clone, field.getBoolean(original))));
        primitiveFieldCopiers.put(byte.class,
            field -> ((original, clone, context) -> field.setByte(clone, field.getByte(original))));
        primitiveFieldCopiers.put(char.class,
            field -> ((original, clone, context) -> field.setChar(clone, field.getChar(original))));
        primitiveFieldCopiers.put(short.class,
            field -> ((original, clone, context) -> field.setShort(clone, field.getShort(original))));
        primitiveFieldCopiers.put(int.class,
            field -> ((original, clone, context) -> field.setInt(clone, field.getInt(original))));
        primitiveFieldCopiers.put(long.class,
            field -> ((original, clone, context) -> field.setLong(clone, field.getLong(original))));
        primitiveFieldCopiers.put(float.class,
            field -> ((original, clone, context) -> field.setFloat(clone, field.getFloat(original))));
        primitiveFieldCopiers.put(double.class,
            field -> ((original, clone, context) -> field.setDouble(clone, field.getDouble(original))));

        PRIMITIVE_FIELD_COPIERS = Collections.unmodifiableMap(primitiveFieldCopiers);
    }

    @Override
    public FieldCopier getFieldCopier(Field field, CopyAction action) {
        Class<?> type = field.getType();
        if (type.isPrimitive()) {
            return PRIMITIVE_FIELD_COPIERS.get(field.getType()).apply(field);
        }
        switch (action) {
            case NULL:
                return (original, clone, context) -> field.set(clone, null);
            case ORIGINAL:
                return (original, clone, context) -> field.set(clone, field.get(original));
            case DEFAULT:
                return (original, clone, context) -> field.set(clone, context.copy(field.get(original)));
            default:
                throw new IllegalStateException();
        }
    }

}
