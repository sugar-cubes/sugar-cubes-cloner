package org.sugarcubes.cloner;

/**
 * @author Maxim Butov
 */
public class Logger {

    public static String toString(Object obj) {
        return obj != null ?
            obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj)) :
            String.valueOf((Object) null);
    }

    public static void log(String format, Object... args) {
        //System.out.println("[" + Thread.currentThread().getName() + "] " + String.format(format, args));
    }

}
