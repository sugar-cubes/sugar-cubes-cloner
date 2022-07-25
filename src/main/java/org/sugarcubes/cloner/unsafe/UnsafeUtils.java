package org.sugarcubes.cloner.unsafe;

import java.lang.reflect.Field;

import org.sugarcubes.cloner.ClonerException;
import org.sugarcubes.cloner.ReflectionUtils;

import sun.misc.Unsafe;

/**
 * Utility class to obtain {@link Unsafe} instance.
 *
 * @author Maxim Butov
 */
public class UnsafeUtils {

    private static final Unsafe UNSAFE;

    static {
        try {
            Field theUnsafe = ReflectionUtils.getField(Unsafe.class, "theUnsafe");
            UNSAFE = (Unsafe) theUnsafe.get(null);
        }
        catch (ReflectiveOperationException e) {
            throw new ClonerException("Cannot load sun.misc.Unsafe instance.", e);
        }
    }

    /**
     * Returns {@link Unsafe} instance.
     *
     * @return {@link Unsafe} instance
     */
    public static Unsafe getUnsafe() {
        return UNSAFE;
    }

}
