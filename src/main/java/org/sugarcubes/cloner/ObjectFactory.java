package org.sugarcubes.cloner;

/**
 * @author Maxim Butov
 */
public interface ObjectFactory<T> {

    T newInstance() throws Exception;

}
