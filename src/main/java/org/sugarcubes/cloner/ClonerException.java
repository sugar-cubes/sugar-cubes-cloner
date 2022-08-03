package org.sugarcubes.cloner;

/**
 * Generic cloner exception. Wraps some happened checked exception.
 *
 * @author Maxim Butov
 */
public class ClonerException extends RuntimeException {

    /**
     * Constructor with message and cause.
     *
     * @param message message
     * @param cause original exception
     */
    public ClonerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with cause.
     *
     * @param cause original exception
     */
    public ClonerException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with message.
     *
     * @param message message
     */
    public ClonerException(String message) {
        super(message);
    }

}
