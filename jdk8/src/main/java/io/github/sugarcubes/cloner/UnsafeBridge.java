/*
 * Copyright 2017-2024 the original author or authors.
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
 * Bridge to Java Unsafe implementation.
 *
 * @see SunMiscUnsafeBridge
 *
 * @author Maxim Butov
 */
interface UnsafeBridge {

    int getInt(Object o, long offset);

    void putInt(Object o, long offset, int x);

    Object getObject(Object o, long offset);

    void putObject(Object o, long offset, Object x);

    boolean getBoolean(Object o, long offset);

    void putBoolean(Object o, long offset, boolean x);

    byte getByte(Object o, long offset);

    void putByte(Object o, long offset, byte x);

    short getShort(Object o, long offset);

    void putShort(Object o, long offset, short x);

    char getChar(Object o, long offset);

    void putChar(Object o, long offset, char x);

    long getLong(Object o, long offset);

    void putLong(Object o, long offset, long x);

    float getFloat(Object o, long offset);

    void putFloat(Object o, long offset, float x);

    double getDouble(Object o, long offset);

    void putDouble(Object o, long offset, double x);

    long objectFieldOffset(Field f);

    <T> T allocateInstance(Class<T> cls) throws InstantiationException;

    void throwException(Throwable ee);

}
