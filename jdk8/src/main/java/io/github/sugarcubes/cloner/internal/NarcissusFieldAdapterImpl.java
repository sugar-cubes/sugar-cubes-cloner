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

import io.github.toolfactory.narcissus.Narcissus;

public class NarcissusFieldAdapterImpl implements FieldAdapter {

    public static final FieldAdapterFactory FACTORY = NarcissusFieldAdapterImpl::new;

    private final Field field;

    public NarcissusFieldAdapterImpl(Field field) {
        this.field = field;
    }

    @Override
    public <T> T getObject(Object target) throws Exception {
        return (T) Narcissus.getObjectField(target, field);
    }

    @Override
    public <T> void setObject(Object target, T value) throws Exception {
        Narcissus.setObjectField(target, field, value);
    }

    @Override
    public int getInt(Object target) throws Exception {
        return Narcissus.getIntField(target, field);
    }

    @Override
    public void setInt(Object target, int value) throws Exception {
        Narcissus.setIntField(target, field, value);
    }

    @Override
    public byte getByte(Object target) throws Exception {
        return Narcissus.getByteField(target, field);
    }

    @Override
    public void setByte(Object target, byte value) throws Exception {
        Narcissus.setByteField(target, field, value);
    }

    @Override
    public boolean getBoolean(Object target) throws Exception {
        return Narcissus.getBooleanField(target, field);
    }

    @Override
    public void setBoolean(Object target, boolean value) throws Exception {
        Narcissus.setBooleanField(target, field, value);
    }

    @Override
    public short getShort(Object target) throws Exception {
        return Narcissus.getShortField(target, field);
    }

    @Override
    public void setShort(Object target, short value) throws Exception {
        Narcissus.setShortField(target, field, value);
    }

    @Override
    public char getChar(Object target) throws Exception {
        return Narcissus.getCharField(target, field);
    }

    @Override
    public void setChar(Object target, char value) throws Exception {
        Narcissus.setCharField(target, field, value);
    }

    @Override
    public long getLong(Object target) throws Exception {
        return Narcissus.getLongField(target, field);
    }

    @Override
    public void setLong(Object target, long value) throws Exception {
        Narcissus.setLongField(target, field, value);
    }

    @Override
    public float getFloat(Object target) throws Exception {
        return Narcissus.getFloatField(target, field);
    }

    @Override
    public void setFloat(Object target, float value) throws Exception {
        Narcissus.setFloatField(target, field, value);
    }

    @Override
    public double getDouble(Object target) throws Exception {
        return Narcissus.getDoubleField(target, field);
    }

    @Override
    public void setDouble(Object target, double value) throws Exception {
        Narcissus.setDoubleField(target, field, value);
    }

}
