package org.sugarcubes.cloner;

/**
 * @author Maxim Butov
 */
public interface ClonerContext {

    <T> T copy(T object) throws Throwable;

}
