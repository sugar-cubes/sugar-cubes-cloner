/*
 * Copyright 2017-2026 the original author or authors.
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
package io.github.sugarcubes.cloner.unsafe;

import java.lang.reflect.Field;

/**
 * Bridge to Java Unsafe implementation: {@code jdk.internal.misc.Unsafe} on JDK 9+ if it is exported to the
 * cloner, {@link sun.misc.Unsafe} otherwise.
 *
 * @see SunMiscUnsafeBridge
 *
 * @author Maxim Butov
 */
public interface UnsafeBridge {

    /**
     * Returns the Unsafe instance behind the bridge.
     *
     * @return {@code sun.misc.Unsafe} or {@code jdk.internal.misc.Unsafe}
     */
    Object getUnsafe();

    /**
     * Reads a {@code int} field.
     *
     * @param o object holding the field, or the base of a static field
     * @param offset field offset
     * @return field value
     */
    int getInt(Object o, long offset);

    /**
     * Writes a {@code int} field.
     *
     * @param o object holding the field, or the base of a static field
     * @param offset field offset
     * @param x new value
     */
    void putInt(Object o, long offset, int x);

    /**
     * Reads a reference field.
     *
     * @param o object holding the field, or the base of a static field
     * @param offset field offset
     * @return field value
     */
    Object getObject(Object o, long offset);

    /**
     * Writes a reference field.
     *
     * @param o object holding the field, or the base of a static field
     * @param offset field offset
     * @param x new value
     */
    void putObject(Object o, long offset, Object x);

    /**
     * Reads a {@code boolean} field.
     *
     * @param o object holding the field, or the base of a static field
     * @param offset field offset
     * @return field value
     */
    boolean getBoolean(Object o, long offset);

    /**
     * Writes a {@code boolean} field.
     *
     * @param o object holding the field, or the base of a static field
     * @param offset field offset
     * @param x new value
     */
    void putBoolean(Object o, long offset, boolean x);

    /**
     * Reads a {@code byte} field.
     *
     * @param o object holding the field, or the base of a static field
     * @param offset field offset
     * @return field value
     */
    byte getByte(Object o, long offset);

    /**
     * Writes a {@code byte} field.
     *
     * @param o object holding the field, or the base of a static field
     * @param offset field offset
     * @param x new value
     */
    void putByte(Object o, long offset, byte x);

    /**
     * Reads a {@code short} field.
     *
     * @param o object holding the field, or the base of a static field
     * @param offset field offset
     * @return field value
     */
    short getShort(Object o, long offset);

    /**
     * Writes a {@code short} field.
     *
     * @param o object holding the field, or the base of a static field
     * @param offset field offset
     * @param x new value
     */
    void putShort(Object o, long offset, short x);

    /**
     * Reads a {@code char} field.
     *
     * @param o object holding the field, or the base of a static field
     * @param offset field offset
     * @return field value
     */
    char getChar(Object o, long offset);

    /**
     * Writes a {@code char} field.
     *
     * @param o object holding the field, or the base of a static field
     * @param offset field offset
     * @param x new value
     */
    void putChar(Object o, long offset, char x);

    /**
     * Reads a {@code long} field.
     *
     * @param o object holding the field, or the base of a static field
     * @param offset field offset
     * @return field value
     */
    long getLong(Object o, long offset);

    /**
     * Writes a {@code long} field.
     *
     * @param o object holding the field, or the base of a static field
     * @param offset field offset
     * @param x new value
     */
    void putLong(Object o, long offset, long x);

    /**
     * Reads a {@code float} field.
     *
     * @param o object holding the field, or the base of a static field
     * @param offset field offset
     * @return field value
     */
    float getFloat(Object o, long offset);

    /**
     * Writes a {@code float} field.
     *
     * @param o object holding the field, or the base of a static field
     * @param offset field offset
     * @param x new value
     */
    void putFloat(Object o, long offset, float x);

    /**
     * Reads a {@code double} field.
     *
     * @param o object holding the field, or the base of a static field
     * @param offset field offset
     * @return field value
     */
    double getDouble(Object o, long offset);

    /**
     * Writes a {@code double} field.
     *
     * @param o object holding the field, or the base of a static field
     * @param offset field offset
     * @param x new value
     */
    void putDouble(Object o, long offset, double x);

    /**
     * Returns the offset of the instance field.
     *
     * @param f instance field
     * @return offset
     */
    long objectFieldOffset(Field f);

    /**
     * Returns the object holding the static field, to be used with {@link #staticFieldOffset(Field)}.
     *
     * @param f static field
     * @return base object
     */
    Object staticFieldBase(Field f);

    /**
     * Returns the offset of the static field in its {@link #staticFieldBase(Field) base object}.
     *
     * @param f static field
     * @return offset
     */
    long staticFieldOffset(Field f);

    /**
     * Creates an instance of the class without calling a constructor.
     *
     * @param <T> object type
     * @param cls class
     * @return new instance with default field values
     * @throws InstantiationException if the class is abstract, an interface or an array
     */
    <T> T allocateInstance(Class<T> cls) throws InstantiationException;

    /**
     * Throws the exception, checked or not, without declaring it.
     *
     * @param ee exception
     */
    void throwException(Throwable ee);

}
