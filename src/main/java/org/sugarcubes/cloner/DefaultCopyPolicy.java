package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Copy policy implementation.
 *
 * @author Maxim Butov
 */
public class DefaultCopyPolicy implements CopyPolicy {

    private final Map<Field, FieldCopyAction> fieldActions = new HashMap<>();

    /**
     * Creates copy policy.
     *
     * @param fieldActions field actions
     */
    public DefaultCopyPolicy(Map<Field, FieldCopyAction> fieldActions) {
        this.fieldActions.putAll(fieldActions);
    }

    @Override
    public FieldCopyAction getFieldAction(Field field) {
        return fieldActions.getOrDefault(field, FieldCopyAction.DEFAULT);
    }

}
