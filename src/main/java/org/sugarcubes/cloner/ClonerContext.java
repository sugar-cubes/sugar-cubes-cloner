package org.sugarcubes.cloner;

/**
 * @author Maxim Butov
 */
public interface ClonerContext extends LaterSupport {

    <T> T copy(T object) throws Throwable;

}
