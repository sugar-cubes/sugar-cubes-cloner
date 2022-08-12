package org.sugarcubes.cloner;

import java.lang.reflect.Field;

/**
 * Field copier factory.
 *
 * @author Maxim Butov
 */
public interface FieldCopierFactory {

    /**
     * Returns specific field copier for the field and the action.
     *
     * @param field field
     * @param policy cloning policy
     * @return field copier
     */
    FieldCopier getFieldCopier(Field field, CloningPolicy policy);

}
