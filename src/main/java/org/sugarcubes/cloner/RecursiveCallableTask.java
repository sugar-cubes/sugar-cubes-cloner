package org.sugarcubes.cloner;

import java.util.concurrent.Callable;
import java.util.concurrent.RecursiveTask;

/**
 * @author Maxim Butov
 */
public abstract class RecursiveCallableTask<V> extends RecursiveTask<V> {

    public static <V> RecursiveCallableTask<V> of(Callable<V> callable) {
        return new RecursiveCallableTask<V>() {
            @Override
            protected V call() throws Exception {
                return callable.call();
            }
        };
    }

    protected abstract V call() throws Exception;

    @Override
    protected V compute() {
        try {
            return call();
        }
        catch (Exception e) {
            throw uncheckedThrow(e);
        }
    }

    private static <T extends Throwable> Error uncheckedThrow(Throwable t) throws T {
        throw (T) t;
    }

}
