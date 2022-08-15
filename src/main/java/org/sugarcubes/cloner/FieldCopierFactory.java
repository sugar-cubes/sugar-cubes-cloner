package org.sugarcubes.cloner;

import java.lang.reflect.Field;

/**
 * Field copier factory.
 *
 * @author Maxim Butov
 */
public interface FieldCopierFactory {

    /**
     * Returns specific field copier for the primitive or object field and the action.
     *
     * @param field  field
     * @param action copying action
     * @return field copy action
     */
    default FieldCopier getFieldCopier(Field field, FieldCopyAction action) {
        return field.getType().isPrimitive() ? getPrimitiveFieldCopier(field) : getObjectFieldCopier(field, action);
    }

    /**
     * Returns field copier for the primitive field.
     *
     * @param field field
     * @return field copier
     */
    FieldCopier getPrimitiveFieldCopier(Field field);

    /**
     * Returns specific field copier for the object field and the action.
     *
     * @param field  field
     * @param action copying action
     * @return field copy action
     */
    FieldCopier getObjectFieldCopier(Field field, FieldCopyAction action);

}
