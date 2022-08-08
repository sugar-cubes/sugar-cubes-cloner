package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.sugarcubes.cloner.Check.argNotNull;
import static org.sugarcubes.cloner.Check.isNull;

public final class CustomCloningPolicy implements CloningPolicy {

    private final Set<Class<?>> immutableTypes = new HashSet<>(IMMUTABLE_TYPES);
    private final LazyCache<Class<?>, CopyAction> typeActions = new LazyCache<>(CloningPolicy.super::getTypeAction);
    private final LazyCache<Field, CopyAction> fieldActions = new LazyCache<>(CloningPolicy.super::getFieldAction);

    /**
     * Registers types as immutable.
     *
     * @param type immutable type
     * @param types immutable types
     * @return same policy instance
     */
    public CustomCloningPolicy immutable(Class<?> type, Class<?>... types) {
        argNotNull(type, "Type");
        immutableTypes.add(type);
        immutableTypes.addAll(Arrays.asList(types));
        return this;
    }

    /**
     * Registers custom action for type.
     *
     * @param type object type
     * @param action custom action
     * @return same cloner instance
     */
    public CustomCloningPolicy type(Class<?> type, CopyAction action) {
        argNotNull(type, "Type");
        argNotNull(action, "Action");
        isNull(typeActions.put(type, action), "Action for %s already set.", type.getName());
        return this;
    }

    /**
     * Registers custom action for field.
     *
     * @param field field
     * @param action custom action
     * @return same cloner instance
     */
    public CustomCloningPolicy field(Field field, CopyAction action) {
        argNotNull(field, "Field");
        argNotNull(action, "Action");
        isNull(fieldActions.put(field, action), "Action for %s already set.", field);
        return this;
    }

    /**
     * Registers custom action for field.
     *
     * @param type object type
     * @param field declared field name
     * @param action custom action
     * @return same cloner instance
     */
    public CustomCloningPolicy field(Class<?> type, String field, CopyAction action) {
        argNotNull(type, "Type");
        argNotNull(field, "Field");
        argNotNull(action, "Action");
        return field(ReflectionUtils.getField(type, field), action);
    }

    @Override
    public CopyAction getFieldAction(Field field) {
        return fieldActions.get(field);
    }

    @Override
    public CopyAction getTypeAction(Class<?> type) {
        return typeActions.get(type);
    }

    @Override
    public boolean isImmutable(Class<?> type) {
        return immutableTypes.contains(type);
    }

}
