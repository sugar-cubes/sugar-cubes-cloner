package io.github.sugarcubes.cloner.internal;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Throwable> {

    T get() throws E;

}
