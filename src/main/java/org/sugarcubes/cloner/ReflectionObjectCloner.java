package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author Maxim Butov
 */
class ReflectionObjectCloner<T> extends TwoPhaseObjectCloner<T> {

    final Class<T> type;
    final FieldCache fieldCache;
    final CloningPolicy policy;
    final ObjectAllocator allocator;

    ReflectionObjectCloner(Class<T> type, FieldCache fieldCache, CloningPolicy policy, ObjectAllocator allocator) {
        this.type = type;
        this.fieldCache = fieldCache;
        this.policy = policy;
        this.allocator = allocator;
    }

    @Override
    public T allocate(T original) throws Throwable {
        return allocator.newInstance(type);
    }

    @Override
    public void deepCopy(T original, T clone, ClonerContext context) throws Throwable {
        for (Map.Entry<Field, CloningAction> entry : fieldCache.getFields(type)) {
            Field field = entry.getKey();
            switch (entry.getValue()) {
                case NULL:
                    field.set(clone, null);
                    break;
                case SKIP:
                    continue;
                case ORIGINAL:
                    field.set(clone, field.get(original));
                    break;
                case DEFAULT:
                    Object originalValue = field.get(original);
                    Object cloneValue;
                    try {
                        cloneValue = context.copy(originalValue);
                    }
                    catch (SkipObject e) {
                        break;
                    }
                    field.set(clone, cloneValue);
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
    }

}
