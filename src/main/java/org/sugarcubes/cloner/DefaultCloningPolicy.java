package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Maxim Butov
 */
public class DefaultCloningPolicy implements CloningPolicy {

    private final Set<Class<?>> immutableClasses = new HashSet<>(IMMUTABLE_CLASSES);

    @Override
    public CloningAction getFieldAction(Field field) {
        return CloningPolicy.super.getFieldAction(field);
    }

    @Override
    public boolean isImmutable(Class<?> type) {
        return immutableClasses.contains(type);
    }

}
