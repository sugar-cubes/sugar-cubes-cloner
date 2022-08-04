package org.sugarcubes.cloner;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Variant of {@link java.util.concurrent.Callable} with any throwable.
 *
 * @param <T> result type
 *
 * @author Maxim Butov
 */
@FunctionalInterface
public interface Executable<T> {

    /**
     * Does the work.
     *
     * @return result of work
     * @throws Throwable if something went wrong
     */
    T execute() throws Throwable;

    /**
     * Runs {@link #execute()} and wraps exceptions into {@link ClonerException}.
     *
     * @return result
     * @throws ClonerException if something went wrong
     */
    default T unchecked() throws ClonerException {
        try {
            return execute();
        }
        catch (Error | ClonerException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new ClonerException(e);
        }
    }

    /**
     * A void-result {@link Executable}.
     *
     * @param <E> exception type
     */
    @FunctionalInterface
    interface Void<E extends Throwable> extends Executable<Object> {

        @Override
        default Object execute() throws E {
            perform();
            return null;
        }

        /**
         * Does the work.
         *
         * @throws E if something went wrong
         */
        void perform() throws E;

        /**
         * Creates {@link Void} by lambda or method reference.
         *
         * @param <E> exception type
         * @param executable lambda or method reference
         * @return executable
         */
        static <E extends Throwable> Void<E> of(Void<E> executable) {
            return executable;
        }

        /**
         * Runs the executable.
         *
         * @param <E> exception type
         * @param executable lambda or method reference
         * @throws ClonerException if something went wrong
         */
        static <E extends Throwable> void unchecked(Void<E> executable) throws ClonerException {
            executable.unchecked();
        }

    }

    /**
     * Creates {@link Executable} by lambda or method reference.
     *
     * @param <T> result type
     * @param <E> exception type
     * @param executable lambda or method reference
     * @return executable
     */
    static <T, E extends Throwable> Executable<T> of(Executable<T> executable) {
        return executable;
    }

    /**
     * Runs the executable.
     *
     * @param <T> result type
     * @param <E> exception type
     * @param executable lambda or method reference
     * @return execution result
     * @throws ClonerException if something went wrong
     */
    static <T, E extends Throwable> T unchecked(Executable<T> executable) throws ClonerException {
        return executable.unchecked();
    }

    /**
     * Converts this into {@link Supplier}.
     *
     * @return supplier
     */
    default Supplier<T> asSupplier() {
        return this::unchecked;
    }

    /**
     * Converts this into {@link Callable}.
     *
     * @return callable
     */
    default Callable<T> asCallable() {
        return this::unchecked;
    }

    /**
     * Converts this into {@link Runnable}.
     *
     * @return runnable
     */
    default Runnable asRunnable() {
        return this::unchecked;
    }

}
