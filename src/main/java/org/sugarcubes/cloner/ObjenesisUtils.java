package org.sugarcubes.cloner;

/**
 * Utility to determine the availability of Objenesis.
 *
 * @author Maxim Butov
 */
public class ObjenesisUtils {

    /**
     * Availability of Objenesis.
     *
     * @return true, if Objenesis library is present in classpath
     */
    public static boolean isObjenesisAvailable() {
        return ReflectionUtils.isClassAvailable("org.objenesis.ObjenesisStd");
    }

    /**
     * Utility class.
     */
    private ObjenesisUtils() {
    }

}
