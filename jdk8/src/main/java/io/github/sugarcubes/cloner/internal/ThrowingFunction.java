package io.github.sugarcubes.cloner.internal;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {

    R apply(T value) throws E;

}
