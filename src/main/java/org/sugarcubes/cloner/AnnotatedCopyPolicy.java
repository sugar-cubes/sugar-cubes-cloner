package org.sugarcubes.cloner;

import java.lang.reflect.Field;

/**
 * Copy policy based on annotations.
 *
 * @author Maxim Butov
 */
public class AnnotatedCopyPolicy implements CopyPolicy {

    @Override
    public CopyAction getTypeAction(Class<?> type) {
        TypePolicy annotation = type.getDeclaredAnnotation(TypePolicy.class);
        if (annotation != null) {
            return annotation.value();
        }
        for (Class<?> t = type; (t = t.getSuperclass()) != null; ) {
            annotation = t.getDeclaredAnnotation(TypePolicy.class);
            if (annotation != null && annotation.applyToSubtypes()) {
                return annotation.value();
            }
        }
        return CopyAction.DEFAULT;
    }

    @Override
    public FieldCopyAction getFieldAction(Field field) {
        FieldPolicy annotation = field.getDeclaredAnnotation(FieldPolicy.class);
        if (annotation != null) {
            return annotation.value();
        }
        return FieldCopyAction.DEFAULT;
    }

}
