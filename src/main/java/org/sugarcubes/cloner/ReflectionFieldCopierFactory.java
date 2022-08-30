package org.sugarcubes.cloner;

import java.lang.reflect.Field;

/**
 * Field copiers factory which uses reflection for getting/setting field values.
 *
 * @author Maxim Butov
 */
public final class ReflectionFieldCopierFactory implements FieldCopierFactory {

    @Override
    public FieldCopier getFieldCopier(Field field, FieldCopyAction action) {
        return FieldCopierFactory.super.getFieldCopier(ReflectionUtils.makeAccessible(field), action);
    }

    @Override
    public FieldCopier getPrimitiveFieldCopier(Field field) {
        switch (field.getType().getName()) {
            case "boolean":
                return (original, clone, context) -> field.setBoolean(clone, field.getBoolean(original));
            case "byte":
                return (original, clone, context) -> field.setByte(clone, field.getByte(original));
            case "char":
                return (original, clone, context) -> field.setChar(clone, field.getChar(original));
            case "short":
                return (original, clone, context) -> field.setShort(clone, field.getShort(original));
            case "int":
                return (original, clone, context) -> field.setInt(clone, field.getInt(original));
            case "long":
                return (original, clone, context) -> field.setLong(clone, field.getLong(original));
            case "float":
                return (original, clone, context) -> field.setFloat(clone, field.getFloat(original));
            case "double":
                return (original, clone, context) -> field.setDouble(clone, field.getDouble(original));
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public FieldCopier getObjectFieldCopier(Field field, FieldCopyAction action) {
        switch (action) {
            case SKIP:
                return FieldCopier.NOOP;
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
