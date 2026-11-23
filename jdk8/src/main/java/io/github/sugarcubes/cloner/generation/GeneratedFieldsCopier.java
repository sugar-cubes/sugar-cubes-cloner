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
package io.github.sugarcubes.cloner.generation;

import io.github.sugarcubes.cloner.CopyContext;
import io.github.sugarcubes.cloner.TwoPhaseObjectCopier;
import io.github.sugarcubes.cloner.spi.FieldAdapter;
import io.github.sugarcubes.cloner.spi.InstanceAllocator;

/**
 * Super class of generated copiers. A generated copier has the private access of the copied class (a hidden class in
 * its nest on JDK 15+, an anonymous class of {@code sun.misc.Unsafe} on JDK 8-14): it reads all fields and writes
 * non-final ones directly; final fields are written by field adapters, since only the declaring class may write them.
 *
 * <p>The copier of a class copies the fields declared by the class, the copier of the super class copies the
 * inherited ones. The class, its constructor and the methods the generated code calls are public: a hidden class may
 * be in another module, an anonymous class is checked for access as its host, which is no subclass of this one.</p>
 *
 * @param <T> type
 * @author Maxim Butov
 */
public abstract class GeneratedFieldsCopier<T> extends TwoPhaseObjectCopier<T> {

    private final InstanceAllocator<T> allocator;

    private final TwoPhaseObjectCopier<? super T> parent;

    private final FieldAdapter[] adapters;

    /**
     * Constructor of the generated class: while a copier lives, the shared cache keeps the class.
     */
    private Object constructor;

    /**
     * Creates the copier.
     *
     * @param allocator object allocator of the type
     * @param parent copier of the super type or {@code null}
     * @param adapters adapters of the final fields, in the order of their indices in the generated code
     */
    public GeneratedFieldsCopier(InstanceAllocator<T> allocator, TwoPhaseObjectCopier<? super T> parent,
        FieldAdapter[] adapters) {
        this.allocator = allocator;
        this.parent = parent;
        this.adapters = adapters;
    }

    /**
     * Keeps the constructor of the generated class alive, see {@link CopierGenerator}.
     *
     * @param constructor constructor of the generated class
     */
    void keep(Object constructor) {
        this.constructor = constructor;
    }

    @Override
    public T allocate(T original) throws Exception {
        return allocator.newInstance();
    }

    @Override
    public void deepCopy(T original, T clone, CopyContext context) throws Exception {
        if (parent != null) {
            parent.deepCopy(original, clone, context);
        }
        copyFields(original, clone, context);
    }

    /**
     * Copies the fields declared by the type, generated.
     *
     * @param original original object
     * @param clone clone
     * @param context copy context
     * @throws Exception if copying fails
     */
    protected abstract void copyFields(Object original, Object clone, CopyContext context) throws Exception;

    /**
     * Sets a non-final reference field by its index when the clone of its value is ready, generated.
     *
     * @param index index of the field
     * @param target clone holding the field
     * @param value clone of the value
     */
    protected abstract void setField(int index, Object target, Object value);

    /**
     * Copies the value of a non-final reference field, subscribes to its clone if it is being created right now.
     *
     * @param context copy context
     * @param value original value
     * @param target clone holding the field
     * @param index index of the field for {@link #setField(int, Object, Object)}
     * @return the clone of the value, or {@code null} if it will be set later
     * @throws Exception if copying fails
     */
    public final Object copyReference(CopyContext context, Object value, Object target, int index) throws Exception {
        Object copy = context.copyNow(value);
        if (copy != CopyContext.PENDING) {
            return copy;
        }
        context.subscribe(value, later -> setField(index, target, later));
        return null;
    }

    /**
     * Copies the value of a final reference field and writes it by the adapter.
     *
     * @param context copy context
     * @param value original value
     * @param target clone holding the field
     * @param adapter index of the adapter
     * @throws Exception if copying fails
     */
    public final void copyFinalReference(CopyContext context, Object value, Object target, int adapter)
        throws Exception {
        Object copy = context.copyNow(value);
        FieldAdapter fieldAdapter = adapters[adapter];
        if (copy != CopyContext.PENDING) {
            fieldAdapter.setObject(target, copy);
        }
        else {
            context.subscribe(value, later -> fieldAdapter.setObject(target, later));
        }
    }

    /**
     * Writes a final reference field.
     *
     * @param adapter index of the adapter
     * @param target object holding the field
     * @param value value
     * @throws Exception if the field cannot be written
     */
    public final void setFinalObject(int adapter, Object target, Object value) throws Exception {
        adapters[adapter].setObject(target, value);
    }

    /**
     * Writes a final {@code boolean} field.
     *
     * @param adapter index of the adapter
     * @param target object holding the field
     * @param value value
     * @throws Exception if the field cannot be written
     */
    public final void setFinalBoolean(int adapter, Object target, boolean value) throws Exception {
        adapters[adapter].setBoolean(target, value);
    }

    /**
     * Writes a final {@code byte} field.
     *
     * @param adapter index of the adapter
     * @param target object holding the field
     * @param value value
     * @throws Exception if the field cannot be written
     */
    public final void setFinalByte(int adapter, Object target, byte value) throws Exception {
        adapters[adapter].setByte(target, value);
    }

    /**
     * Writes a final {@code char} field.
     *
     * @param adapter index of the adapter
     * @param target object holding the field
     * @param value value
     * @throws Exception if the field cannot be written
     */
    public final void setFinalChar(int adapter, Object target, char value) throws Exception {
        adapters[adapter].setChar(target, value);
    }

    /**
     * Writes a final {@code short} field.
     *
     * @param adapter index of the adapter
     * @param target object holding the field
     * @param value value
     * @throws Exception if the field cannot be written
     */
    public final void setFinalShort(int adapter, Object target, short value) throws Exception {
        adapters[adapter].setShort(target, value);
    }

    /**
     * Writes a final {@code int} field.
     *
     * @param adapter index of the adapter
     * @param target object holding the field
     * @param value value
     * @throws Exception if the field cannot be written
     */
    public final void setFinalInt(int adapter, Object target, int value) throws Exception {
        adapters[adapter].setInt(target, value);
    }

    /**
     * Writes a final {@code long} field.
     *
     * @param adapter index of the adapter
     * @param target object holding the field
     * @param value value
     * @throws Exception if the field cannot be written
     */
    public final void setFinalLong(int adapter, Object target, long value) throws Exception {
        adapters[adapter].setLong(target, value);
    }

    /**
     * Writes a final {@code float} field.
     *
     * @param adapter index of the adapter
     * @param target object holding the field
     * @param value value
     * @throws Exception if the field cannot be written
     */
    public final void setFinalFloat(int adapter, Object target, float value) throws Exception {
        adapters[adapter].setFloat(target, value);
    }

    /**
     * Writes a final {@code double} field.
     *
     * @param adapter index of the adapter
     * @param target object holding the field
     * @param value value
     * @throws Exception if the field cannot be written
     */
    public final void setFinalDouble(int adapter, Object target, double value) throws Exception {
        adapters[adapter].setDouble(target, value);
    }

}
