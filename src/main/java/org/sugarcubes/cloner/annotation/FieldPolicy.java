package org.sugarcubes.cloner.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.sugarcubes.cloner.CopyAction;
import org.sugarcubes.cloner.FieldCopyAction;

/**
 * Annotation for applying {@link CopyAction} to fields.
 *
 * @author Maxim Butov
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldPolicy {

    /**
     * Copy action for the field.
     *
     * @return copy action
     */
    FieldCopyAction value();

}
