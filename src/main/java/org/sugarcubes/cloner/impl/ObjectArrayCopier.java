package org.sugarcubes.cloner.impl;

import java.lang.reflect.Array;

/**
 * @author Maxim Butov
 */
public final class ObjectArrayCopier extends TwoPhaseObjectCopier<Object[]> {

    @Override
    public Object[] allocate(Object[] original) throws Throwable {
        Class<?> componentType = original.getClass().getComponentType();
        return componentType == Object.class ? new Object[original.length] :
            (Object[]) Array.newInstance(componentType, original.length);
    }

    @Override
    public void deepCopy(Object[] original, Object[] clone, CopyContext context) throws Throwable {
        for (int k = 0; k < original.length; k++) {
            try {
                clone[k] = context.copy(original[k]);
            }
            catch (SkipObject e) {
                // continue
            }
        }
    }

}
