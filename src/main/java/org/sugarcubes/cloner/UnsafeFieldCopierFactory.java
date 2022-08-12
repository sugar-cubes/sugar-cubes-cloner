package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import sun.misc.Unsafe;

/**
 * Field copier factory which creates unsafe field copiers. Unsafe field copiers work faster because
 * the {@link Unsafe} does not check type of the object when setting field value.
 *
 * @author Maxim Butov
 */
public class UnsafeFieldCopierFactory implements FieldCopierFactory {

    /**
     * {@link Unsafe} instance.
     */
    private static final Unsafe UNSAFE = UnsafeUtils.getUnsafe();

    /**
     * Function long -&gt; FieldCopier.
     */
    @FunctionalInterface
    private interface FieldCopierByOffset {

        /**
         * Returns field copier by offset.
         *
         * @param offset field offset
         * @return field copier
         */
        FieldCopier get(long offset);

    }

    /**
     * Primitive field copiers factories.
     */
    private static final Map<Class<?>, FieldCopierByOffset> PRIMITIVE_FIELD_COPIERS;

    static {
        Map<Class<?>, FieldCopierByOffset> primitiveFieldCopiers = new HashMap<>();

        primitiveFieldCopiers.put(boolean.class,
            offset -> ((original, clone, context) -> UNSAFE.putBoolean(clone, offset, UNSAFE.getBoolean(original, offset))));
        primitiveFieldCopiers.put(byte.class,
            offset -> ((original, clone, context) -> UNSAFE.putByte(clone, offset, UNSAFE.getByte(original, offset))));
        primitiveFieldCopiers.put(char.class,
            offset -> ((original, clone, context) -> UNSAFE.putChar(clone, offset, UNSAFE.getChar(original, offset))));
        primitiveFieldCopiers.put(short.class,
            offset -> ((original, clone, context) -> UNSAFE.putShort(clone, offset, UNSAFE.getShort(original, offset))));
        primitiveFieldCopiers.put(int.class,
            offset -> ((original, clone, context) -> UNSAFE.putInt(clone, offset, UNSAFE.getInt(original, offset))));
        primitiveFieldCopiers.put(long.class,
            offset -> ((original, clone, context) -> UNSAFE.putLong(clone, offset, UNSAFE.getLong(original, offset))));
        primitiveFieldCopiers.put(float.class,
            offset -> ((original, clone, context) -> UNSAFE.putFloat(clone, offset, UNSAFE.getFloat(original, offset))));
        primitiveFieldCopiers.put(double.class,
            offset -> ((original, clone, context) -> UNSAFE.putDouble(clone, offset, UNSAFE.getDouble(original, offset))));

        PRIMITIVE_FIELD_COPIERS = Collections.unmodifiableMap(primitiveFieldCopiers);
    }

    /**
     * Returns offset of the field.
     *
     * @param field field
     * @return offset
     */
    private static long getOffset(Field field) {
        return UNSAFE.objectFieldOffset(field);
    }

    @Override
    public FieldCopier getFieldCopier(Field field, CopyAction action) {
        Class<?> type = field.getType();
        long offset = getOffset(field);
        if (type.isPrimitive()) {
            return PRIMITIVE_FIELD_COPIERS.get(field.getType()).get(offset);
        }
        switch (action) {
            case NULL:
                return (original, clone, context) -> UNSAFE.putObject(clone, offset, null);
            case ORIGINAL:
                return (original, clone, context) -> UNSAFE.putObject(clone, offset, UNSAFE.getObject(original, offset));
            case DEFAULT:
                return (original, clone, context) ->
                    UNSAFE.putObject(clone, offset, context.copy(UNSAFE.getObject(original, offset)));
            default:
                throw new IllegalStateException();
        }
    }

}
