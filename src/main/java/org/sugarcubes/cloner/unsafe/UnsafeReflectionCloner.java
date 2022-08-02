package org.sugarcubes.cloner.unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.sugarcubes.cloner.CloningPolicy;
import org.sugarcubes.cloner.CopyAction;
import org.sugarcubes.cloner.CopyContext;
import org.sugarcubes.cloner.ReflectionCloner;

import sun.misc.Unsafe;

/**
 * Cloner implementation which uses {@link Unsafe} to create abjects and copy fields.
 *
 * @author Maxim Butov
 */
public class UnsafeReflectionCloner extends ReflectionCloner {

    /**
     * Constructor.
     */
    public UnsafeReflectionCloner() {
        super(new UnsafeObjectAllocator());
    }

    /**
     * Constructor.
     */
    public UnsafeReflectionCloner(CloningPolicy policy) {
        super(new UnsafeObjectAllocator(), policy);
    }

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

    private static <X> TernaryConsumer<Object, Long, Object> getCopyOperation(TernaryConsumer<Object, Long, X> unsafeSetter,
        BiFunction<Object, Long, X> unsafeGetter) {
        return (from, offset, into) -> unsafeSetter.accept(into, offset, unsafeGetter.apply(from, offset));
    }

    /**
     * Mapping of primitive types to copy operations.
     */
    private static final Map<Class<?>, TernaryConsumer<Object, Long, Object>> COPY_OPERATIONS = new HashMap<>();

    static {
        COPY_OPERATIONS.put(boolean.class, getCopyOperation(UNSAFE::putBoolean, UNSAFE::getBoolean));
        COPY_OPERATIONS.put(byte.class, getCopyOperation(UNSAFE::putByte, UNSAFE::getByte));
        COPY_OPERATIONS.put(char.class, getCopyOperation(UNSAFE::putChar, UNSAFE::getChar));
        COPY_OPERATIONS.put(short.class, getCopyOperation(UNSAFE::putShort, UNSAFE::getShort));
        COPY_OPERATIONS.put(int.class, getCopyOperation(UNSAFE::putInt, UNSAFE::getInt));
        COPY_OPERATIONS.put(long.class, getCopyOperation(UNSAFE::putLong, UNSAFE::getLong));
        COPY_OPERATIONS.put(float.class, getCopyOperation(UNSAFE::putFloat, UNSAFE::getFloat));
        COPY_OPERATIONS.put(double.class, getCopyOperation(UNSAFE::putDouble, UNSAFE::getDouble));
    }

    @Override
    protected void copyField(Object original, Object clone, Field field, CopyAction action, CopyContext context)
        throws Throwable {
        long offset = UNSAFE.objectFieldOffset(field);
        switch (action) {
            case NULL:
                UNSAFE.putObject(clone, offset, null);
                break;
            case ORIGINAL:
                TernaryConsumer<Object, Long, Object> primitiveCopy = COPY_OPERATIONS.get(field.getType());
                if (primitiveCopy != null) {
                    primitiveCopy.accept(original, offset, clone);
                }
                else {
                    UNSAFE.putObject(clone, offset, UNSAFE.getObject(original, offset));
                }
                break;
            case DEFAULT:
                UNSAFE.putObject(clone, offset, context.copy(UNSAFE.getObject(original, offset)));
                break;
            default:
                throw new IllegalStateException();
        }
    }

}
