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
package io.github.sugarcubes.cloner.engine;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.spi.FieldAdapter;
import io.github.sugarcubes.cloner.util.Checks;
import io.github.sugarcubes.cloner.util.PrimitiveType;

/**
 * Values which clones get in fields instead of the copies of the original values, set by
 * {@link io.github.sugarcubes.cloner.ReflectionClonerBuilder#fieldValue(Field, Supplier)}. A value has priority over
 * the actions and policies of the field.
 *
 * @author Maxim Butov
 */
@FunctionalInterface
public interface FieldValues {

    /**
     * No values.
     */
    FieldValues NONE = field -> null;

    /**
     * Returns the supplier of the value of the field.
     *
     * @param field field
     * @return supplier, called for every clone, or {@code null} if the field is copied by its action
     */
    Supplier<?> valueOf(Field field);

    /**
     * Checks that the value fits the field: it is an instance of the type of the field or its wrapper, not
     * {@code null} for a primitive field.
     *
     * @param field field
     * @param value value
     * @return the value
     * @throws ClonerException if the value does not fit
     */
    static Object check(Field field, Object value) {
        Class<?> type = field.getType();
        if (value == null ? type.isPrimitive() : !PrimitiveType.wrapper(type).isInstance(value)) {
            throw new ClonerException(String.format("The value %s does not fit field %s.", value, field));
        }
        return value;
    }

    /**
     * Writes the value to the field with the setter of the field type.
     *
     * @param adapter field adapter
     * @param field field
     * @param target object
     * @param value value, checked by {@link #check(Field, Object)}
     * @throws Exception if the field cannot be written
     */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    static void write(FieldAdapter adapter, Field field, Object target, Object value) throws Exception {
        PrimitiveType primitive = PrimitiveType.of(field.getType());
        if (primitive == null) {
            adapter.setObject(target, value);
            return;
        }
        switch (primitive) {
            case BOOLEAN:
                adapter.setBoolean(target, (Boolean) value);
                break;
            case CHAR:
                adapter.setChar(target, (Character) value);
                break;
            case BYTE:
                adapter.setByte(target, (Byte) value);
                break;
            case SHORT:
                adapter.setShort(target, (Short) value);
                break;
            case INT:
                adapter.setInt(target, (Integer) value);
                break;
            case LONG:
                adapter.setLong(target, (Long) value);
                break;
            case FLOAT:
                adapter.setFloat(target, (Float) value);
                break;
            case DOUBLE:
                adapter.setDouble(target, (Double) value);
                break;
            default:
                throw Checks.mustNotHappen();
        }
    }

}
