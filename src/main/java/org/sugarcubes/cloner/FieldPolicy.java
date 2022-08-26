package org.sugarcubes.cloner;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for applying {@link FieldCopyAction} to fields.
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
