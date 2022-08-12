package org.sugarcubes.cloner;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Result of copy process. Contains copy instance (which may be not fully copied) and optional action to complete copying.
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

    /**
     * Next action to complete copying.
     */
    private final Callable<?> next;

    /**
     * Creates an instant copy result.
     *
     * @param object resulting object
     */
    public CopyResult(T object) {
        this(object, null);
    }

    /**
     * Creates a copy result with next action.
     *
     * @param object resulting object
     * @param next action to complete copying
     */
    public CopyResult(T object, Callable<?> next) {
        this.object = object;
        this.next = next;
    }

    /**
     * Returns resulting object.
     *
     * @return resulting object
     */
    public T getObject() {
        return object;
    }

    /**
     * Applies action to the {@link #next} if it is present.
     *
     * @param action action to apply
     */
    public void ifHasNext(Consumer<Callable<?>> action) {
        if (next != null) {
            action.accept(next);
        }
    }

}
