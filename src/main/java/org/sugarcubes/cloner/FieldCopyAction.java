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
     * Return {@code null} instead of original object. This action cannot be applied to primitive fields.
     */
    NULL,

    /**
     * Return the original object.
     */
    ORIGINAL,

    /**
     * Clone the object, if it is not immutable.
     */
    DEFAULT,

}
