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
     * A void result {@link Executable}.
     */
    @FunctionalInterface
    interface Void extends Executable<Object> {

        @Override
        default Object execute() throws Throwable {
            perform();
            return null;
        }

        /**
         * Does the work.
         *
         * @throws Throwable if something went wrong
         */
        void perform() throws Throwable;

        /**
         * Creates {@link Void} by lambda or method reference.
         *
         * @param executable lambda or method reference
         * @return executable
         */
        static Void of(Void executable) {
            return executable;
        }

        /**
         * Runs the executable.
         *
         * @param <E> exception type
         * @param executable lambda or method reference
         * @throws ClonerException if something went wrong
         */
        static <E extends Throwable> void unchecked(Void executable) throws ClonerException {
            executable.unchecked();
        }

    }

    /**
     * Creates {@link Executable} by lambda or method reference.
     *
     * @param <T> result type
     * @param executable lambda or method reference
     * @return executable
     */
    static <T> Executable<T> of(Executable<T> executable) {
        return executable;
    }

    /**
     * Runs the executable.
     *
     * @param <T> result type
     * @param executable lambda or method reference
     * @return execution result
     * @throws ClonerException if something went wrong
     */
    static <T> T unchecked(Executable<T> executable) throws ClonerException {
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
