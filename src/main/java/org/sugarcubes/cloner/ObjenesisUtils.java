package org.sugarcubes.cloner;

/**
 * Utility to determine the availability of Objenesis.
 *
 * @author Maxim Butov
 */
public class ObjenesisUtils {

    private static final boolean OBJENESIS_AVAILABLE;

    static {
        boolean available;
        try {
            Class.forName("org.objenesis.ObjenesisStd");
            available = true;
        }
        catch (ClassNotFoundException e) {
            available = false;
        }
        OBJENESIS_AVAILABLE = available;
    }

    /**
     * Availability of Objenesis.
     *
     * @return true, if Objenesis library is present in classpath
     */
    public static boolean isObjenesisAvailable() {
        return OBJENESIS_AVAILABLE;
    }

}
