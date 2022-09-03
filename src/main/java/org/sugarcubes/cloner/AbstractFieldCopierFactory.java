package org.sugarcubes.cloner;

import java.lang.reflect.Field;

/**
 * Base field copier factory.
 *
 * @author Maxim Butov
 */
public abstract class AbstractFieldCopierFactory implements FieldCopierFactory {

    @Override
    public FieldCopier getFieldCopier(Field field, FieldCopyAction action) {
        if (action == FieldCopyAction.SKIP) {
            return FieldCopier.NOOP;
        }
        if (field.getType().isPrimitive()) {
            Check.illegalArg(action == FieldCopyAction.NULL, "Cannot apply action NULL for primitive field %s.", field);
            return getPrimitiveFieldCopier(field);
        }
        return getObjectFieldCopier(field, action);
    }

    /**
     * Returns field copier for the primitive field.
     *
     * @param field field
     * @return field copier
     */
    protected abstract FieldCopier getPrimitiveFieldCopier(Field field);

    /**
     * Returns specific field copier for the object field and the action.
     *
     * @param field field
     * @param action copying action
     * @return field copy action
     */
    protected abstract FieldCopier getObjectFieldCopier(Field field, FieldCopyAction action);

}
