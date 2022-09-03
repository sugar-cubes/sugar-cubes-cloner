package org.sugarcubes.cloner;

/**
 * Actions which can be applied to objects.
 *
 * @author Maxim Butov
 */
public enum CopyAction {

    /**
     * Use {@code null} instead of original object.
     */
    NULL,

    /**
     * Use the original object.
     */
    ORIGINAL,

    /**
     * Clone the object, if it is not immutable.
     */
    DEFAULT,

}
