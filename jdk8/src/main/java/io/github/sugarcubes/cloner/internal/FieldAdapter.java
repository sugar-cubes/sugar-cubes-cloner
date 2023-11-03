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

public interface FieldAdapter {

    <T> T getObject(Object target) throws Exception;

    <T> void setObject(Object target, T value) throws Exception;

    int getInt(Object target) throws Exception;

    void setInt(Object target, int value) throws Exception;

    byte getByte(Object target) throws Exception;

    void setByte(Object target, byte value) throws Exception;

    boolean getBoolean(Object target) throws Exception;

    void setBoolean(Object target, boolean value) throws Exception;

    short getShort(Object target) throws Exception;

    void setShort(Object target, short value) throws Exception;

    char getChar(Object target) throws Exception;

    void setChar(Object target, char value) throws Exception;

    long getLong(Object target) throws Exception;

    void setLong(Object target, long value) throws Exception;

    float getFloat(Object target) throws Exception;

    void setFloat(Object target, float value) throws Exception;

    double getDouble(Object target) throws Exception;

    void setDouble(Object target, double value) throws Exception;

}
