package org.sugarcubes.cloner;

import java.lang.reflect.Array;

/**
 * Two-phase copier of an array of objects.
 *
 * @author Maxim Butov
 */
public class ObjectArrayCopier implements TwoPhaseObjectCopier<Object[]> {

    @Override
    public Object[] allocate(Object[] original) {
        Class<?> componentType = original.getClass().getComponentType();
        return componentType == Object.class ? new Object[original.length] :
            (Object[]) Array.newInstance(componentType, original.length);
    }

    @Override
    public void deepCopy(Object[] original, Object[] clone, CopyContext context) throws Exception {
        for (int k = 0, length = original.length; k < length; k++) {
            clone[k] = context.copy(original[k]);
        }
    }

}
