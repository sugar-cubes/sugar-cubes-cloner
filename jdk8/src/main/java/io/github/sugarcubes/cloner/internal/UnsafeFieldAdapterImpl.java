/*
 * Copyright 2017-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
