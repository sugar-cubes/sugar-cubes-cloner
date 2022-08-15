package org.sugarcubes.cloner;

/**
 * Arguments checking utilities.
 *
 * @author Maxim Butov
 */
public class Check {

    /**
     * Throws {@link IllegalArgumentException}.
     *
     * @param format format for exception message
     * @param args arguments for exception message
     * @return nothing, always throws exception
     */
    public static Error illegalArg(String format, Object... args) throws IllegalArgumentException {
        throw new IllegalArgumentException(args.length != 0 ? String.format(format, args) : format);
    }

    /**
     * Throws {@link IllegalArgumentException} if condition is true.
     *
     * @param condition condition
     * @param format format for exception message
     * @param args arguments for exception message
     */
    public static void illegalArg(boolean condition, String format, Object... args) throws IllegalArgumentException {
        if (condition) {
            throw illegalArg(format, args);
        }
    }

    /**
     * Checks the value is not null or throws exception.
     *
     * @param <T> object type
     * @param value value
     * @param format format for exception message
     * @param args arguments for exception message
     * @return value
     * @throws IllegalArgumentException if {@code value == null}
     */
    public static <T> T notNull(T value, String format, Object... args) throws IllegalArgumentException {
        illegalArg(value == null, format, args);
        return value;
    }

    /**
     * Checks the value is not null or throws exception.
     *
     * @param <T> object type
     * @param value value
     * @param name argument name
     * @return value
     * @throws IllegalArgumentException if {@code value == null}
     */
    public static <T> T argNotNull(T value, String name) throws IllegalArgumentException {
        return notNull(value, "%s must be not null", name);
    }

    /**
     * Checks the value is null or throws exception.
     *
     * @param value value
     * @param format format for exception message
     * @param args arguments for exception message
     * @throws IllegalArgumentException if {@code value != null}
     */
    public static void isNull(Object value, String format, Object... args) throws IllegalArgumentException {
        illegalArg(value != null, format, args);
    }

    /**
     * Utility class.
     */
    private Check() {
    }

}
