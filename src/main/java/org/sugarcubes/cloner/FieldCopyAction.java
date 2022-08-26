package org.sugarcubes.cloner;

/**
 * Actions which applied to fields.
 *
 * @author Maxim Butov
 */
public enum FieldCopyAction {

    /**
     * Skip field.
     */
    SKIP,

    /**
     * Set {@code null} into clone's field value. This action cannot be applied to primitive fields.
     */
    NULL,

    /**
     * Set into clone's field value from original's field.
     */
    ORIGINAL,

    /**
     * Clone the object, if it is not immutable.
     */
    DEFAULT,

}
