package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Copy policy implementation with annotation processing.
 *
 * @author Maxim Butov
 */
public class AnnotatedCopyPolicy extends DefaultCopyPolicy {

    /**
     * Creates copy policy.
     *
     * @param fieldActions field actions
     */
    public AnnotatedCopyPolicy(Map<Class<?>, CopyAction> typeActions, Map<Field, FieldCopyAction> fieldActions) {
        super(typeActions, fieldActions);
    }

    @Override
    public FieldCopyAction getFieldAction(Field field) {
        FieldCopyAction action = super.getFieldAction(field);
        if (action == FieldCopyAction.DEFAULT) {
            FieldPolicy annotation = field.getDeclaredAnnotation(FieldPolicy.class);
            if (annotation != null) {
                return annotation.value();
            }
        }
        return action;
    }

    @Override
    public CopyAction getTypeAction(Class<?> type) {
        CopyAction action = super.getTypeAction(type);
        if (action == CopyAction.DEFAULT) {
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
        }
        return action;
    }

}
