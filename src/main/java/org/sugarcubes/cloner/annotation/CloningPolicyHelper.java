package org.sugarcubes.cloner.annotation;

import java.lang.reflect.Field;

import org.sugarcubes.cloner.CopyAction;
import org.sugarcubes.cloner.CopyPolicy;

/**
 * Methods which do not belong to {@link CopyPolicy} interface but are (re)used in the code.
 *
 * @author Maxim Butov
 */
public class CloningPolicyHelper {

    private static CopyAction getFromAnnotationOrDefault(Field field) {
        FieldPolicy annotation = field.getDeclaredAnnotation(FieldPolicy.class);
        return annotation != null ? annotation.value() : CopyAction.DEFAULT;
    }

    private static CopyAction getFromAnnotationOrDefault(Class<?> type) {
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

    /**
     * Utility class.
     */
    private CloningPolicyHelper() {
    }
}
