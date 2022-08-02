package org.sugarcubes.cloner;

/**
 * A variant of {@link Runnable} with declared exception.
 *
 * @author Maxim Butov
 */
@FunctionalInterface
interface UnsafeRunnable {

    /**
     * Some code.
     *
     * @throws Throwable if something went wrong
     */
    void run() throws Throwable;

}
