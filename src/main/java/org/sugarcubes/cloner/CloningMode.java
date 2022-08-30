package org.sugarcubes.cloner;

/**
 * Cloner work mode.
 *
 * @author Maxim Butov
 */
public enum CloningMode {

    /**
     * One thread with recursion.
     */
    RECURSIVE,

    /**
     * One thread, no recursion.
     */
    SEQUENTIAL,

    /**
     * Several threads.
     */
    PARALLEL,

}
