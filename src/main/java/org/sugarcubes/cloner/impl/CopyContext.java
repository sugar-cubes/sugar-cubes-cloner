package org.sugarcubes.cloner.impl;

/**
 * @author Maxim Butov
 */
public interface CopyContext {

    void invokeLater(UnsafeRunnable runnable);

    <T> T copy(T object) throws Throwable;

    void complete() throws Throwable;

}
