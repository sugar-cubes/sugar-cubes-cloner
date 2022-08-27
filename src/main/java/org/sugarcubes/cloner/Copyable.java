package org.sugarcubes.cloner;

/**
 * An interface for classes which know how to clone themselves. Similar to {@link Cloneable}.
 *
 * @author Maxim Butov
 */
public interface Copyable<T> {

    /**
     * Creates deep copy of this object.
     *
     * @param context copying context
     * @return clone of this object
     */
    T copy(CopyContext context);

}
