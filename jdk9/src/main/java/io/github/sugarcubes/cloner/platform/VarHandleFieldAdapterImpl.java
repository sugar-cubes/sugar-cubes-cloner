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
package io.github.sugarcubes.cloner.platform;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.Optional;

import io.github.sugarcubes.cloner.spi.FieldAdapter;
import io.github.sugarcubes.cloner.spi.FieldAdapterFactory;
import io.github.sugarcubes.cloner.util.ClonerExceptionUtils;

/**
 * Field adapter which uses {@link VarHandle} obtained from the trusted {@link MethodHandles.Lookup}, so it can also
 * write final fields.
 *
 * @author Maxim Butov
 */
class VarHandleFieldAdapterImpl implements FieldAdapter {

    /**
     * Factory of the adapters.
     */
    public static final FieldAdapterFactory FACTORY = field -> Optional.of(new VarHandleFieldAdapterImpl(field));

    private static final MethodHandles.Lookup LOOKUP = TrustedLookup.byReflection();

    private final VarHandle handle;

    /**
     * Constructor.
     *
     * @param field field
     */
    public VarHandleFieldAdapterImpl(Field field) {
        this.handle = ClonerExceptionUtils.replaceException(() -> LOOKUP.unreflectVarHandle(field));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObject(Object target) {
        return (T) handle.get(target);
    }

    @Override
    public <T> void setObject(Object target, T value) {
        handle.set(target, value);
    }

    @Override
    public int getInt(Object target) {
        return (int) handle.get(target);
    }

    @Override
    public void setInt(Object target, int value) {
        handle.set(target, value);
    }

    @Override
    public byte getByte(Object target) {
        return (byte) handle.get(target);
    }

    @Override
    public void setByte(Object target, byte value) {
        handle.set(target, value);
    }

    @Override
    public boolean getBoolean(Object target) {
        return (boolean) handle.get(target);
    }

    @Override
    public void setBoolean(Object target, boolean value) {
        handle.set(target, value);
    }

    @Override
    public short getShort(Object target) {
        return (short) handle.get(target);
    }

    @Override
    public void setShort(Object target, short value) {
        handle.set(target, value);
    }

    @Override
    public char getChar(Object target) {
        return (char) handle.get(target);
    }

    @Override
    public void setChar(Object target, char value) {
        handle.set(target, value);
    }

    @Override
    public long getLong(Object target) {
        return (long) handle.get(target);
    }

    @Override
    public void setLong(Object target, long value) {
        handle.set(target, value);
    }

    @Override
    public float getFloat(Object target) {
        return (float) handle.get(target);
    }

    @Override
    public void setFloat(Object target, float value) {
        handle.set(target, value);
    }

    @Override
    public double getDouble(Object target) {
        return (double) handle.get(target);
    }

    @Override
    public void setDouble(Object target, double value) {
        handle.set(target, value);
    }

}
