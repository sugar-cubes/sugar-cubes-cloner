package org.sugarcubes.cloner;

/**
 * Marker interface for trivial copiers. "Trivial" means that repeated calling of the copy method is cheaper than saving
 * and requesting previous result from cache.
 *
 * @author Maxim Butov
 */
public interface TrivialCopier<T> extends ObjectCopier<T> {

}
