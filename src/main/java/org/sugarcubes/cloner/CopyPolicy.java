package org.sugarcubes.cloner;

import java.lang.reflect.Field;

/**
 * Set of rules for objects cloning.
 *
 * @author Maxim Butov
 */
public interface CopyPolicy {

    /**
     * Returns action to apply to a field value. Must return non-null value.
     *
     * @param field field
     * @return action
     */
    default FieldCopyAction getFieldAction(Field field) {
        return FieldCopyAction.DEFAULT;
    }

    /**
     * Returns action to apply to an instance of the type. Must return non-null value.
     *
     * @param type type
     * @return action
     */
    default CopyAction getTypeAction(Class<?> type) {
        return CopyAction.DEFAULT;
    }

}
