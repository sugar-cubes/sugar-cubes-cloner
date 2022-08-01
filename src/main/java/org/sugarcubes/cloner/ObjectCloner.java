package org.sugarcubes.cloner;

/**
 * @author Maxim Butov
 */
public interface ObjectCloner<T> {

    ObjectCloner<?> NULL = new NullCloner<>();
    ObjectCloner<?> NOOP = new NoopCloner<>();
    ObjectCloner<?> SHALLOW = new ShallowCloner<>();
    ObjectCloner<?> OBJECT_ARRAY = new ObjectArrayCloner();

    boolean isTrivial();

    T clone(T original, ClonerContext context) throws Throwable;

}
