package org.sugarcubes.cloner;

/**
 * @author Maxim Butov
 */
public interface LaterSupport {

    void invokeLater(UnsafeRunnable runnable);

    void complete() throws Throwable;

}
