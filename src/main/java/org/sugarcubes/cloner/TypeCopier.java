package org.sugarcubes.cloner;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for applying {@link ObjectCopier} to type.
 *
 * @author Maxim Butov
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TypeCopier {

    /**
     * Copier class for the type. Class must have no-arg constructor.
     *
     * @return copier class
     */
    Class<ObjectCopier<?>> value();

}
