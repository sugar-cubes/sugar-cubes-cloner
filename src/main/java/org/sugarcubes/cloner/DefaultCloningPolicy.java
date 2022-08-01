package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mutable {@link CloningPolicy}.
 *
 * @author Maxim Butov
 */
public class DefaultCloningPolicy implements CloningPolicy {

    /**
     * Set of immutable types.
     */
    private Set<Class<?>> immutableClasses = new HashSet<>(IMMUTABLE_CLASSES);

    private Map<Class<?>, CopyAction> typeActions = new HashMap<>();

    private Map<Field, CopyAction> fieldActions = new HashMap<>();

    public Set<Class<?>> getImmutableClasses() {
        return immutableClasses;
    }

    public void setImmutableClasses(Set<Class<?>> immutableClasses) {
        this.immutableClasses = immutableClasses;
    }

    public Map<Class<?>, CopyAction> getTypeActions() {
        return typeActions;
    }

    public void setTypeActions(Map<Class<?>, CopyAction> typeActions) {
        this.typeActions = typeActions;
    }

    public Map<Field, CopyAction> getFieldActions() {
        return fieldActions;
    }

    public void setFieldActions(Map<Field, CopyAction> fieldActions) {
        this.fieldActions = fieldActions;
    }

    @Override
    public CopyAction getFieldAction(Field field) {
        return fieldActions.computeIfAbsent(field, CloningPolicy.super::getFieldAction);
    }

    @Override
    public CopyAction getTypeAction(Class<?> type) {
        return typeActions.computeIfAbsent(type, CloningPolicy.super::getTypeAction);
    }

    @Override
    public boolean isImmutable(Class<?> type) {
        return immutableClasses.contains(type);
    }

}
