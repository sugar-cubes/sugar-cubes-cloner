package org.sugarcubes.cloner;

/**
 * Generic cloner exception. Wraps some happened checked exception.
 *
 * @author Maxim Butov
 */
public class ClonerException extends RuntimeException {

    /**
     * Constructor with message and cause.
     */
    public ClonerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with cause.
     */
    public ClonerException(Throwable cause) {
        super(cause);
    }

}
