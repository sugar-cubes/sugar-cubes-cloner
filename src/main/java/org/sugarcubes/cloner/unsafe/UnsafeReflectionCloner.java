package org.sugarcubes.cloner.unsafe;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import sun.misc.Unsafe;

/**
 * Cloner implementation which uses {@link Unsafe} to create abjects and copy fields.
 *
 * @author Maxim Butov
 */
public class UnsafeReflectionCloner {//extends ReflectionCloner {

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

    /**
     * Default constructor.
     */
    public UnsafeReflectionCloner() {
//        super(new UnsafeObjectAllocator());
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

/*
    @Override
    protected void copyField(Object from, Object into, Field field, ClonerContext context) throws Throwable {
        long offset = UNSAFE.objectFieldOffset(field);
        TernaryConsumer<Object, Long, Object> primitiveCopy = OPERATIONS_WITH_PRIMITIVES.get(field.getType());
        if (primitiveCopy != null) {
            primitiveCopy.accept(from, offset, into);
        }
        else {
            UNSAFE.putObject(into, offset, context.copy(UNSAFE.getObject(from, offset)));
        }
    }
*/

}
