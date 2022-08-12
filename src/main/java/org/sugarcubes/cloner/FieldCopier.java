package org.sugarcubes.cloner;

/**
 * Field copier interface.
 *
 * @author Maxim Butov
 */
public interface FieldCopier {

    /**
     * Copies the field value from the original object to the clone.
     *
     * @param original original object
     * @param clone clone
     * @param context copying context
     * @throws Exception if something went wrong
     */
    void copy(Object original, Object clone, CopyContext context) throws Exception;

}
