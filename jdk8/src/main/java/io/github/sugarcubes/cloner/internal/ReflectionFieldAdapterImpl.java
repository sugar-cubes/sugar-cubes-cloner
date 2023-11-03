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

/**
 * todo
 */
public class ReflectionFieldAdapterImpl implements FieldAdapter {

    public static final FieldAdapterFactory FACTORY = ReflectionFieldAdapterImpl::new;

    private final Field field;

    public ReflectionFieldAdapterImpl(Field field) {
        Checks.argNotNull(field, "Field");
        this.field = field;
    }

    @Override
    public <T> T getObject(Object target) throws Exception {
        return (T) field.get(target);
    }

    @Override
    public <T> void setObject(Object target, T value) throws Exception {
        field.set(target, value);
    }

    @Override
    public int getInt(Object target) throws Exception {
        return field.getInt(target);
    }

    @Override
    public void setInt(Object target, int value) throws Exception {
        field.setInt(target, value);
    }

    @Override
    public byte getByte(Object target) throws Exception {
        return field.getByte(target);
    }

    @Override
    public void setByte(Object target, byte value) throws Exception {
        field.setByte(target, value);
    }

    @Override
    public boolean getBoolean(Object target) throws Exception {
        return field.getBoolean(target);
    }

    @Override
    public void setBoolean(Object target, boolean value) throws Exception {
        field.setBoolean(target, value);
    }

    @Override
    public short getShort(Object target) throws Exception {
        return field.getShort(target);
    }

    @Override
    public void setShort(Object target, short value) throws Exception {
        field.setShort(target, value);
    }

    @Override
    public char getChar(Object target) throws Exception {
        return field.getChar(target);
    }

    @Override
    public void setChar(Object target, char value) throws Exception {
        field.setChar(target, value);
    }

    @Override
    public long getLong(Object target) throws Exception {
        return field.getLong(target);
    }

    @Override
    public void setLong(Object target, long value) throws Exception {
        field.setLong(target, value);
    }

    @Override
    public float getFloat(Object target) throws Exception {
        return field.getFloat(target);
    }

    @Override
    public void setFloat(Object target, float value) throws Exception {
        field.setFloat(target, value);
    }

    @Override
    public double getDouble(Object target) throws Exception {
        return field.getDouble(target);
    }

    @Override
    public void setDouble(Object target, double value) throws Exception {
        field.setDouble(target, value);
    }

}
