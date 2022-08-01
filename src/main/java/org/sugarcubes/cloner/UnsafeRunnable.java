package org.sugarcubes.cloner;

/**
 * @author Maxim Butov
 */
@FunctionalInterface
interface UnsafeRunnable {

    void run(ClonerContext context) throws Throwable;

}
