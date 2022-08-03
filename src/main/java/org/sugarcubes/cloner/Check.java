package org.sugarcubes.cloner;

/**
 * Arguments checking utilities.
 *
 * @author Maxim Butov
 */
public class Check {

    /**
     * Checks the value is not null or throws exception.
     *
     * @param <T> object type
     * @param value value
     * @param message format for exception message
     * @param args arguments for exception message
     * @return value
     * @throws IllegalArgumentException if {@code value == null}
     */
    public static <T> T notNull(T value, String message, Object... args) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException(String.format(message, args));
        }
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
     * @param message format for exception message
     * @param args arguments for exception message
     * @throws IllegalArgumentException if {@code value != null}
     */
    public static void isNull(Object value, String message, Object... args) throws IllegalArgumentException {
        if (value != null) {
            throw new IllegalStateException(String.format(message, args));
        }
    }

    /**
     * Utility class.
     */
    private Check() {
    }

}
