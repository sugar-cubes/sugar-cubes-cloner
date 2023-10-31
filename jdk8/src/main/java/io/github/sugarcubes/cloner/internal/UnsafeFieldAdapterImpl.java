package io.github.sugarcubes.cloner.internal;

import java.lang.reflect.Field;

import io.github.sugarcubes.cloner.Checks;
import sun.misc.Unsafe;

public class UnsafeFieldAdapterImpl implements FieldAdapter {

    public static FieldAdapterFactory getFactory(Unsafe unsafe) {
        return field -> new UnsafeFieldAdapterImpl(unsafe, field);
    }

    private final Unsafe unsafe;
    private final Field field;
    private final long offset;

    public UnsafeFieldAdapterImpl(Unsafe unsafe, Field field) {
        Checks.argNotNull(unsafe, "Unsafe");
        Checks.argNotNull(field, "Field");
        this.unsafe = unsafe;
        this.field = field;
        this.offset = unsafe.objectFieldOffset(field);
    }

    @Override
    public <T> T getObject(Object target) throws Exception {
        return (T) unsafe.getObject(target, offset);
    }

    @Override
    public <T> void setObject(Object target, T value) throws Exception {
        unsafe.putObject(target, offset, value);
    }

    @Override
    public int getInt(Object target) throws Exception {
        return unsafe.getInt(target, offset);
    }

    @Override
    public void setInt(Object target, int value) throws Exception {
        unsafe.putInt(target, offset, value);
    }

    @Override
    public byte getByte(Object target) throws Exception {
        return unsafe.getByte(target, offset);
    }

    @Override
    public void setByte(Object target, byte value) throws Exception {
        unsafe.putByte(target, offset, value);
    }

    @Override
    public boolean getBoolean(Object target) throws Exception {
        return unsafe.getBoolean(target, offset);
    }

    @Override
    public void setBoolean(Object target, boolean value) throws Exception {
        unsafe.putBoolean(target, offset, value);
    }

    @Override
    public short getShort(Object target) throws Exception {
        return unsafe.getShort(target, offset);
    }

    @Override
    public void setShort(Object target, short value) throws Exception {
        unsafe.putShort(target, offset, value);
    }

    @Override
    public char getChar(Object target) throws Exception {
        return unsafe.getChar(target, offset);
    }

    @Override
    public void setChar(Object target, char value) throws Exception {
        unsafe.putChar(target, offset, value);
    }

    @Override
    public long getLong(Object target) throws Exception {
        return unsafe.getLong(target, offset);
    }

    @Override
    public void setLong(Object target, long value) throws Exception {
        unsafe.putLong(target, offset, value);
    }

    @Override
    public float getFloat(Object target) throws Exception {
        return unsafe.getFloat(target, offset);
    }

    @Override
    public void setFloat(Object target, float value) throws Exception {
        unsafe.putFloat(target, offset, value);
    }

    @Override
    public double getDouble(Object target) throws Exception {
        return unsafe.getDouble(target, offset);
    }

    @Override
    public void setDouble(Object target, double value) throws Exception {
        unsafe.putDouble(target, offset, value);
    }

}
