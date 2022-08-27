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
        CopyAction action = typeActions.get(type);
        if (action != null) {
            return action;
        }
        TypePolicy annotation = type.getDeclaredAnnotation(TypePolicy.class);
        if (annotation != null) {
            return annotation.value();
        }
        for (Class<?> t = type; (t = t.getSuperclass()) != null; ) {
            annotation = t.getDeclaredAnnotation(TypePolicy.class);
            if (annotation != null && annotation.includeSubclasses()) {
                return annotation.value();
            }
        }
        return CopyAction.DEFAULT;
    }

    @Override
    public FieldCopyAction getFieldAction(Field field) {
        FieldCopyAction action = fieldActions.get(field);
        if (action != null) {
            return action;
        }
        FieldPolicy annotation = field.getDeclaredAnnotation(FieldPolicy.class);
        if (annotation != null) {
            return annotation.value();
        }
        return FieldCopyAction.DEFAULT;
    }

}
