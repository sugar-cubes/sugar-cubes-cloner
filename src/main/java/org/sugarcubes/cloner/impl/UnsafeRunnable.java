package org.sugarcubes.cloner.impl;

/**
 * @author Maxim Butov
 */
@FunctionalInterface
interface UnsafeRunnable {

    void run() throws Throwable;

}
