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

    private final Map<Class<?>, CopyAction> typeActions;
    private final Map<Field, FieldCopyAction> fieldActions;

    /**
     * Creates copy policy.
     *
     * @param fieldActions field actions
     */
    public DefaultCopyPolicy(Map<Class<?>, CopyAction> typeActions, Map<Field, FieldCopyAction> fieldActions) {
        this.typeActions = new HashMap<>(typeActions);
        this.fieldActions = new HashMap<>(fieldActions);
    }

    @Override
    public CopyAction getTypeAction(Class<?> type) {
        return typeActions.getOrDefault(type, CopyAction.DEFAULT);
    }

    @Override
    public FieldCopyAction getFieldAction(Field field) {
        return fieldActions.getOrDefault(field, FieldCopyAction.DEFAULT);
    }

}
