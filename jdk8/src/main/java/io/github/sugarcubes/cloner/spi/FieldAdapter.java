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
package io.github.sugarcubes.cloner.spi;

/**
 * Reads and writes one field of objects, created for the field by a {@link FieldAdapterFactory}. Every accessor
 * matches the field type: {@code getInt}/{@code setInt} for an {@code int} field, {@code getObject}/{@code setObject}
 * for a reference field and so on. Final fields are written too: the cloner sets them on fresh copies only.
 *
 * @author Maxim Butov
 */
public interface FieldAdapter {

    /**
     * Reads the value of the reference field.
     *
     * @param <T> value type
     * @param target object holding the field
     * @return field value
     * @throws Exception if the field cannot be accessed
     */
    <T> T getObject(Object target) throws Exception;

    /**
     * Writes the value of the reference field.
     *
     * @param <T> value type
     * @param target object holding the field
     * @param value new value
     * @throws Exception if the field cannot be accessed
     */
    <T> void setObject(Object target, T value) throws Exception;

    /**
     * Reads the value of the {@code int} field.
     *
     * @param target object holding the field
     * @return field value
     * @throws Exception if the field cannot be accessed
     */
    int getInt(Object target) throws Exception;

    /**
     * Writes the value of the {@code int} field.
     *
     * @param target object holding the field
     * @param value new value
     * @throws Exception if the field cannot be accessed
     */
    void setInt(Object target, int value) throws Exception;

    /**
     * Reads the value of the {@code byte} field.
     *
     * @param target object holding the field
     * @return field value
     * @throws Exception if the field cannot be accessed
     */
    byte getByte(Object target) throws Exception;

    /**
     * Writes the value of the {@code byte} field.
     *
     * @param target object holding the field
     * @param value new value
     * @throws Exception if the field cannot be accessed
     */
    void setByte(Object target, byte value) throws Exception;

    /**
     * Reads the value of the {@code boolean} field.
     *
     * @param target object holding the field
     * @return field value
     * @throws Exception if the field cannot be accessed
     */
    boolean getBoolean(Object target) throws Exception;

    /**
     * Writes the value of the {@code boolean} field.
     *
     * @param target object holding the field
     * @param value new value
     * @throws Exception if the field cannot be accessed
     */
    void setBoolean(Object target, boolean value) throws Exception;

    /**
     * Reads the value of the {@code short} field.
     *
     * @param target object holding the field
     * @return field value
     * @throws Exception if the field cannot be accessed
     */
    short getShort(Object target) throws Exception;

    /**
     * Writes the value of the {@code short} field.
     *
     * @param target object holding the field
     * @param value new value
     * @throws Exception if the field cannot be accessed
     */
    void setShort(Object target, short value) throws Exception;

    /**
     * Reads the value of the {@code char} field.
     *
     * @param target object holding the field
     * @return field value
     * @throws Exception if the field cannot be accessed
     */
    char getChar(Object target) throws Exception;

    /**
     * Writes the value of the {@code char} field.
     *
     * @param target object holding the field
     * @param value new value
     * @throws Exception if the field cannot be accessed
     */
    void setChar(Object target, char value) throws Exception;

    /**
     * Reads the value of the {@code long} field.
     *
     * @param target object holding the field
     * @return field value
     * @throws Exception if the field cannot be accessed
     */
    long getLong(Object target) throws Exception;

    /**
     * Writes the value of the {@code long} field.
     *
     * @param target object holding the field
     * @param value new value
     * @throws Exception if the field cannot be accessed
     */
    void setLong(Object target, long value) throws Exception;

    /**
     * Reads the value of the {@code float} field.
     *
     * @param target object holding the field
     * @return field value
     * @throws Exception if the field cannot be accessed
     */
    float getFloat(Object target) throws Exception;

    /**
     * Writes the value of the {@code float} field.
     *
     * @param target object holding the field
     * @param value new value
     * @throws Exception if the field cannot be accessed
     */
    void setFloat(Object target, float value) throws Exception;

    /**
     * Reads the value of the {@code double} field.
     *
     * @param target object holding the field
     * @return field value
     * @throws Exception if the field cannot be accessed
     */
    double getDouble(Object target) throws Exception;

    /**
     * Writes the value of the {@code double} field.
     *
     * @param target object holding the field
     * @param value new value
     * @throws Exception if the field cannot be accessed
     */
    void setDouble(Object target, double value) throws Exception;

}
