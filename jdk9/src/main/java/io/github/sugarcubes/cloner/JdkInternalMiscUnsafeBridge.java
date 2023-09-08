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
package io.github.sugarcubes.cloner;

import java.lang.reflect.Field;

/**
 * Implementation of {@link UnsafeBridge} which calls {@link jdk.internal.misc.Unsafe}.
 *
 * @author Maxim Butov
 */
class JdkInternalMiscUnsafeBridge implements UnsafeBridge {

    private final jdk.internal.misc.Unsafe unsafe = jdk.internal.misc.Unsafe.getUnsafe();

    public JdkInternalMiscUnsafeBridge() {
        // check it works
        ClonerExceptionUtils.replaceException(() -> allocateInstance(Object.class));
    }

    @Override
    public int getInt(Object o, long offset) {
        return unsafe.getInt(o, offset);
    }

    @Override
    public void putInt(Object o, long offset, int x) {
        unsafe.putInt(o, offset, x);
    }

    @Override
    public Object getObject(Object o, long offset) {
        return unsafe.getObject(o, offset);
    }

    @Override
    public void putObject(Object o, long offset, Object x) {
        unsafe.putObject(o, offset, x);
    }

    @Override
    public boolean getBoolean(Object o, long offset) {
        return unsafe.getBoolean(o, offset);
    }

    @Override
    public void putBoolean(Object o, long offset, boolean x) {
        unsafe.putBoolean(o, offset, x);
    }

    @Override
    public byte getByte(Object o, long offset) {
        return unsafe.getByte(o, offset);
    }

    @Override
    public void putByte(Object o, long offset, byte x) {
        unsafe.putByte(o, offset, x);
    }

    @Override
    public short getShort(Object o, long offset) {
        return unsafe.getShort(o, offset);
    }

    @Override
    public void putShort(Object o, long offset, short x) {
        unsafe.putShort(o, offset, x);
    }

    @Override
    public char getChar(Object o, long offset) {
        return unsafe.getChar(o, offset);
    }

    @Override
    public void putChar(Object o, long offset, char x) {
        unsafe.putChar(o, offset, x);
    }

    @Override
    public long getLong(Object o, long offset) {
        return unsafe.getLong(o, offset);
    }

    @Override
    public void putLong(Object o, long offset, long x) {
        unsafe.putLong(o, offset, x);
    }

    @Override
    public float getFloat(Object o, long offset) {
        return unsafe.getFloat(o, offset);
    }

    @Override
    public void putFloat(Object o, long offset, float x) {
        unsafe.putFloat(o, offset, x);
    }

    @Override
    public double getDouble(Object o, long offset) {
        return unsafe.getDouble(o, offset);
    }

    @Override
    public void putDouble(Object o, long offset, double x) {
        unsafe.putDouble(o, offset, x);
    }

    @Override
    public long objectFieldOffset(Field f) {
        return unsafe.objectFieldOffset(f);
    }

    @Override
    public <T> T allocateInstance(Class<T> cls) throws InstantiationException {
        return (T) unsafe.allocateInstance(cls);
    }

    @Override
    public void throwException(Throwable ee) {
        unsafe.throwException(ee);
    }

}
