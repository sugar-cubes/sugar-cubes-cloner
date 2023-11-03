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

import io.github.sugarcubes.cloner.AbstractFieldCopierFactory;
import io.github.sugarcubes.cloner.Checks;
import io.github.sugarcubes.cloner.CopyAction;
import io.github.sugarcubes.cloner.FieldCopier;

public class FieldAdapterFieldCopierFactory extends AbstractFieldCopierFactory {

    private final FieldAdapterFactory fieldAdapterFactory;

    public FieldAdapterFieldCopierFactory(FieldAdapterFactory fieldAdapterFactory) {
        Checks.argNotNull(fieldAdapterFactory, "Field adapter factory");
        this.fieldAdapterFactory = fieldAdapterFactory;
    }

    @Override
    protected FieldCopier getPrimitiveFieldCopier(Field field) {
        FieldAdapter fieldAdapter = fieldAdapterFactory.newAdapter(field);
        switch (field.getType().getName()) {
            case "boolean":
                return (original, clone, context) -> fieldAdapter.setBoolean(clone, fieldAdapter.getBoolean(original));
            case "byte":
                return (original, clone, context) -> fieldAdapter.setByte(clone, fieldAdapter.getByte(original));
            case "char":
                return (original, clone, context) -> fieldAdapter.setChar(clone, fieldAdapter.getChar(original));
            case "short":
                return (original, clone, context) -> fieldAdapter.setShort(clone, fieldAdapter.getShort(original));
            case "int":
                return (original, clone, context) -> fieldAdapter.setInt(clone, fieldAdapter.getInt(original));
            case "long":
                return (original, clone, context) -> fieldAdapter.setLong(clone, fieldAdapter.getLong(original));
            case "float":
                return (original, clone, context) -> fieldAdapter.setFloat(clone, fieldAdapter.getFloat(original));
            case "double":
                return (original, clone, context) -> fieldAdapter.setDouble(clone, fieldAdapter.getDouble(original));
            default:
                throw Checks.mustNotHappen();
        }
    }

    @Override
    protected FieldCopier getObjectFieldCopier(Field field, CopyAction action) {
        FieldAdapter fieldAdapter = fieldAdapterFactory.newAdapter(field);
        switch (action) {
            case SKIP:
                return FieldCopier.NOOP;
            case NULL:
                return (original, clone, context) -> fieldAdapter.setObject(clone, null);
            case ORIGINAL:
                return (original, clone, context) -> fieldAdapter.setObject(clone, fieldAdapter.getObject(original));
            case DEFAULT:
                return (original, clone, context) ->
                    fieldAdapter.setObject(clone, context.copy(fieldAdapter.getObject(original)));
            default:
                throw Checks.mustNotHappen();
        }
    }
    
}
