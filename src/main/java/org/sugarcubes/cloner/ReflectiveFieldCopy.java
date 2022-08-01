package org.sugarcubes.cloner;

import java.lang.reflect.Field;

/**
 * @author Maxim Butov
 */
public interface ReflectiveFieldCopy {

    void copyField(Object original, Object clone, Field field, CloningAction action, ClonerContext context) throws Throwable;

}
