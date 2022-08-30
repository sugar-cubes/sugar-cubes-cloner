package org.sugarcubes.cloner;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * Field copier factory which creates unsafe field copiers. Unsafe field copiers work faster because
 * the {@link Unsafe} does not check type of the object when setting field value.
 *
 * @author Maxim Butov
 */
public final class UnsafeFieldCopierFactory implements FieldCopierFactory {

    /**
     * {@link Unsafe} instance.
     */
    private static final Unsafe UNSAFE = UnsafeUtils.getUnsafe();

    @Override
    public FieldCopier getPrimitiveFieldCopier(Field field) {
        long offset = UNSAFE.objectFieldOffset(field);
        switch (field.getType().getName()) {
            case "boolean":
                return (original, clone, context) -> UNSAFE.putBoolean(clone, offset, UNSAFE.getBoolean(original, offset));
            case "byte":
                return (original, clone, context) -> UNSAFE.putByte(clone, offset, UNSAFE.getByte(original, offset));
            case "char":
                return (original, clone, context) -> UNSAFE.putChar(clone, offset, UNSAFE.getChar(original, offset));
            case "short":
                return (original, clone, context) -> UNSAFE.putShort(clone, offset, UNSAFE.getShort(original, offset));
            case "int":
                return (original, clone, context) -> UNSAFE.putInt(clone, offset, UNSAFE.getInt(original, offset));
            case "long":
                return (original, clone, context) -> UNSAFE.putLong(clone, offset, UNSAFE.getLong(original, offset));
            case "float":
                return (original, clone, context) -> UNSAFE.putFloat(clone, offset, UNSAFE.getFloat(original, offset));
            case "double":
                return (original, clone, context) -> UNSAFE.putDouble(clone, offset, UNSAFE.getDouble(original, offset));
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public FieldCopier getObjectFieldCopier(Field field, FieldCopyAction action) {
        long offset = UNSAFE.objectFieldOffset(field);
        switch (action) {
            case SKIP:
                return FieldCopier.NOOP;
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
