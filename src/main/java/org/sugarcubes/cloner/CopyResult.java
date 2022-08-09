package org.sugarcubes.cloner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Result of copy process. Contains copy instance (which may be not fully copied) and list of actions to complete copying.
 *
 * @param <T> object type
 *
 * @author Maxim Butov
 */
public class CopyResult<T> {

    /**
     * Copy of the original object.
     */
    private final T object;
    private final List<Callable<?>> next;

    public CopyResult(T object) {
        this.object = object;
        this.next = Collections.emptyList();
    }

    public CopyResult(T object, Callable<?> next) {
        this.object = object;
        this.next = Collections.singletonList(next);
    }

    public CopyResult(T object, Callable<?>... next) {
        this.object = object;
        this.next = Collections.unmodifiableList(Arrays.asList(next));
    }

    public T getObject() {
        return object;
    }

    public List<Callable<?>> getNext() {
        return next;
    }

}
