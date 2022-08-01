package org.sugarcubes.cloner;

/**
 * Actions which can be applied to inner objects.
 *
 * @author Maxim Butov
 */
public enum CopyAction {

    /**
     * Return {@code null} instead of original object.
     */
    NULL,

    /**
     * Skip the object. This action is suitable for fields.
     */
    SKIP,

    /**
     * Return the original object.
     */
    ORIGINAL,

    /**
     * Clone the object, if it is not immutable.
     */
    DEFAULT,

}
