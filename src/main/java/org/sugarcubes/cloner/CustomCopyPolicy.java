package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Copy policy implementation.
 *
 * @author Maxim Butov
 */
public class CustomCopyPolicy implements CopyPolicy {

    /**
     * Map (type, action).
     */
    private final Map<Class<?>, CopyAction> typeActions;

    /**
     * Map (field, action).
     */
    private final Map<Field, FieldCopyAction> fieldActions;

    /**
     * Creates copy policy.
     *
     * @param typeActions  type actions
     * @param fieldActions field actions
     */
    public CustomCopyPolicy(Map<Class<?>, CopyAction> typeActions, Map<Field, FieldCopyAction> fieldActions) {
        this.typeActions = copyOfMap(typeActions);
        this.fieldActions = copyOfMap(fieldActions);
    }

    private static <K, V> Map<K, V> copyOfMap(Map<K, V> map) {
        Map<K, V> copy = new HashMap<>(map);
        copy.entrySet().removeIf(entry -> entry.getValue() == CopyAction.DEFAULT);
        return copy.isEmpty() ? Collections.emptyMap() : copy;
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
