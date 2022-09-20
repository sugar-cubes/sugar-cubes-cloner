/*
 * Copyright 2017-2022 the original author or authors.
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
package org.sugarcubes.cloner;

import java.lang.reflect.Field;

/**
 * Field copiers factory which uses reflection for getting/setting field values.
 *
 * @author Maxim Butov
 */
public final class ReflectionFieldCopierFactory extends AbstractFieldCopierFactory {

    @Override
    public FieldCopier getFieldCopier(Field field, CopyAction action) {
        return super.getFieldCopier(ReflectionUtils.makeAccessible(field), action);
    }

    @Override
    protected FieldCopier getPrimitiveFieldCopier(Field field) {
        switch (field.getType().getName()) {
            case "boolean":
                return (original, clone, context) -> field.setBoolean(clone, field.getBoolean(original));
            case "byte":
                return (original, clone, context) -> field.setByte(clone, field.getByte(original));
            case "char":
                return (original, clone, context) -> field.setChar(clone, field.getChar(original));
            case "short":
                return (original, clone, context) -> field.setShort(clone, field.getShort(original));
            case "int":
                return (original, clone, context) -> field.setInt(clone, field.getInt(original));
            case "long":
                return (original, clone, context) -> field.setLong(clone, field.getLong(original));
            case "float":
                return (original, clone, context) -> field.setFloat(clone, field.getFloat(original));
            case "double":
                return (original, clone, context) -> field.setDouble(clone, field.getDouble(original));
            default:
                throw Check.neverHappens();
        }
    }

    @Override
    protected FieldCopier getObjectFieldCopier(Field field, CopyAction action) {
        switch (action) {
            case SKIP:
                return FieldCopier.NOOP;
            case NULL:
                return (original, clone, context) -> field.set(clone, null);
            case ORIGINAL:
                return (original, clone, context) -> field.set(clone, field.get(original));
            case DEFAULT:
                return (original, clone, context) -> field.set(clone, context.copy(field.get(original)));
            default:
                throw Check.neverHappens();
        }
    }

}
