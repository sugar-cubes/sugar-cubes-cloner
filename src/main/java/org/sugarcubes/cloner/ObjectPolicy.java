package org.sugarcubes.cloner;

/**
 * Rules for objects cloning. Using {@link ObjectPolicy} significantly slows down cloning process, that's why it is extracted to
 * separate interface and should not be used when {@link CopyPolicy} is enough.
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
