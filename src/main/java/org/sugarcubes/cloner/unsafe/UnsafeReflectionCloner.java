package org.sugarcubes.cloner.unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.sugarcubes.cloner.impl.CopyContext;
import org.sugarcubes.cloner.CopyAction;
import org.sugarcubes.cloner.ObjectAllocator;
import org.sugarcubes.cloner.impl.ReflectionCloner;
import org.sugarcubes.cloner.impl.SkipObject;

import sun.misc.Unsafe;

/**
 * Cloner implementation which uses {@link Unsafe} to create abjects and copy fields.
 *
 * @author Maxim Butov
 */
public class UnsafeReflectionCloner extends ReflectionCloner {

    /**
     * {@link Unsafe} instance.
     */
    private static final Unsafe UNSAFE = UnsafeUtils.getUnsafe();

    /**
     * Three-arguments consumer.
     */
    interface TernaryConsumer<T, U, V> {

        void accept(T t, U u, V v);

    }

    @Override
    protected ObjectAllocator createAllocator() {
        return new UnsafeObjectAllocator();
    }

    private static <X> TernaryConsumer<Object, Long, Object> getCopyOperation(TernaryConsumer<Object, Long, X> unsafeSetter,
        BiFunction<Object, Long, X> unsafeGetter) {
        return (from, offset, into) -> unsafeSetter.accept(into, offset, unsafeGetter.apply(from, offset));
    }

    /**
     * Mapping of primitive types to copy operations.
     */
    private static final Map<Class<?>, TernaryConsumer<Object, Long, Object>> OPERATIONS_WITH_PRIMITIVES = new HashMap<>();

    static {
        OPERATIONS_WITH_PRIMITIVES.put(boolean.class, getCopyOperation(UNSAFE::putBoolean, UNSAFE::getBoolean));
        OPERATIONS_WITH_PRIMITIVES.put(byte.class, getCopyOperation(UNSAFE::putByte, UNSAFE::getByte));
        OPERATIONS_WITH_PRIMITIVES.put(char.class, getCopyOperation(UNSAFE::putChar, UNSAFE::getChar));
        OPERATIONS_WITH_PRIMITIVES.put(short.class, getCopyOperation(UNSAFE::putShort, UNSAFE::getShort));
        OPERATIONS_WITH_PRIMITIVES.put(int.class, getCopyOperation(UNSAFE::putInt, UNSAFE::getInt));
        OPERATIONS_WITH_PRIMITIVES.put(long.class, getCopyOperation(UNSAFE::putLong, UNSAFE::getLong));
        OPERATIONS_WITH_PRIMITIVES.put(float.class, getCopyOperation(UNSAFE::putFloat, UNSAFE::getFloat));
        OPERATIONS_WITH_PRIMITIVES.put(double.class, getCopyOperation(UNSAFE::putDouble, UNSAFE::getDouble));
    }

    @Override
    protected void copyField(Object original, Object clone, Field field, CopyAction action, CopyContext context) throws Throwable {
        if (action == CopyAction.SKIP) {
            return;
        }
        long offset = UNSAFE.objectFieldOffset(field);
        switch (action) {
            case NULL:
                UNSAFE.putObject(clone, offset, null);
                break;
            case ORIGINAL:
                TernaryConsumer<Object, Long, Object> primitiveCopy = OPERATIONS_WITH_PRIMITIVES.get(field.getType());
                if (primitiveCopy != null) {
                    primitiveCopy.accept(original, offset, clone);
                }
                else {
                    UNSAFE.putObject(clone, offset, UNSAFE.getObject(original, offset));
                }
                break;
            case DEFAULT:
                Object originalValue = UNSAFE.getObject(original, offset);
                Object cloneValue;
                try {
                    cloneValue = context.copy(originalValue);
                }
                catch (SkipObject e) {
                    return;
                }
                UNSAFE.putObject(clone, offset, cloneValue);
                break;
            default:
                throw new IllegalStateException();
        }
    }

}
