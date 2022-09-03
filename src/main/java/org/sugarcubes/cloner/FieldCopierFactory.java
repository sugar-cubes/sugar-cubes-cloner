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
     * @param field field
     * @param action copying action
     * @return field copier
     */
    FieldCopier getFieldCopier(Field field, FieldCopyAction action);

}
