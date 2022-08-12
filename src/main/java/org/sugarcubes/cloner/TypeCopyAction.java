package org.sugarcubes.cloner;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for applying {@link CopyAction} to types.
 *
 * @author Maxim Butov
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TypeCopyAction {

    /**
     * Copy action for the type.
     *
     * @return copy action
     */
    CopyAction value();

    /**
     * Apply the action for subtypes.
     *
     * @return apply for subtypes
     */
    boolean includeSubclasses() default false;

}
