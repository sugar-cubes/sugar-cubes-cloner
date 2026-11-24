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
package io.github.sugarcubes.cloner.fields;

import java.lang.reflect.Field;

import io.github.sugarcubes.cloner.spi.FieldAdapter;
import io.github.sugarcubes.cloner.spi.FieldAdapterFactory;
import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.util.Checks;

/**
 * Field adapter which uses {@link Field} methods. The field must be accessible.
 *
 * @author Maxim Butov
 */
public class ReflectionFieldAdapterImpl implements FieldAdapter {

    /**
     * Returns factory of adapters for fields made accessible by the reflection.
     *
     * @param reflection reflection which makes fields accessible
     * @return field adapter factory
     */
    public static FieldAdapterFactory factory(Reflection reflection) {
        return field -> reflection.tryMakeAccessible(field).map(ReflectionFieldAdapterImpl::new);
    }

    private final Field field;

    /**
     * Constructor.
     *
     * @param field accessible field
     */
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
