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

    private final Map<Class<?>, CopyAction> typeActions = new HashMap<>();
    private final Map<Field, FieldCopyAction> fieldActions = new HashMap<>();

    /**
     * Creates copy policy.
     *
     * @param typeActions type actions
     * @param fieldActions field actions
     */
    public DefaultCopyPolicy(Map<Class<?>, CopyAction> typeActions, Map<Field, FieldCopyAction> fieldActions) {
        this.typeActions.putAll(typeActions);
        this.fieldActions.putAll(fieldActions);
    }

    @Override
    public FieldCopyAction getFieldAction(Field field) {
        return fieldActions.getOrDefault(field, FieldCopyAction.DEFAULT);
    }

    @Override
    public CopyAction getTypeAction(Class<?> type) {
        return type.isEnum() ? CopyAction.ORIGINAL : typeActions.getOrDefault(type, CopyAction.DEFAULT);
    }

}
