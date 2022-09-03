package org.sugarcubes.cloner;

/**
 * Rules for objects cloning. Using {@link ObjectPolicy} significantly slows down cloning process,
 * that's why it is extracted to separate interface.
 *
 * @see CopyPolicy
 *
 * @author Maxim Butov
 */
public interface ObjectPolicy {

    /**
     * Returns action to apply to the object. Must return non-null value.
     *
     * @param original original object
     * @return action
     */
    CopyAction getObjectAction(Object original);

}
