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
package io.github.sugarcubes.cloner;

import java.lang.reflect.Field;

import sun.misc.Unsafe;


/**
 * Field copier factory which creates unsafe field copiers. Unsafe field copiers work faster because
 * the {@link Unsafe} does not check type of the object when setting field value.
 *
 * @author Maxim Butov
 */
public final class UnsafeFieldCopierFactory extends AbstractFieldCopierFactory {

    /**
     * {@link UnsafeBridge} instance.
     */
    private final UnsafeBridge unsafe = JdkConfigurationHolder.CONFIGURATION.getUnsafe();

    @Override
    protected FieldCopier getPrimitiveFieldCopier(Field field) {
        long offset = unsafe.objectFieldOffset(field);
        switch (field.getType().getName()) {
            case "boolean":
                return (original, clone, context) -> unsafe.putBoolean(clone, offset, unsafe.getBoolean(original, offset));
            case "byte":
                return (original, clone, context) -> unsafe.putByte(clone, offset, unsafe.getByte(original, offset));
            case "char":
                return (original, clone, context) -> unsafe.putChar(clone, offset, unsafe.getChar(original, offset));
            case "short":
                return (original, clone, context) -> unsafe.putShort(clone, offset, unsafe.getShort(original, offset));
            case "int":
                return (original, clone, context) -> unsafe.putInt(clone, offset, unsafe.getInt(original, offset));
            case "long":
                return (original, clone, context) -> unsafe.putLong(clone, offset, unsafe.getLong(original, offset));
            case "float":
                return (original, clone, context) -> unsafe.putFloat(clone, offset, unsafe.getFloat(original, offset));
            case "double":
                return (original, clone, context) -> unsafe.putDouble(clone, offset, unsafe.getDouble(original, offset));
            default:
                throw Checks.mustNotHappen();
        }
    }

    @Override
    protected FieldCopier getObjectFieldCopier(Field field, CopyAction action) {
        long offset = unsafe.objectFieldOffset(field);
        switch (action) {
            case SKIP:
                return FieldCopier.NOOP;
            case NULL:
                return (original, clone, context) -> unsafe.putObject(clone, offset, null);
            case ORIGINAL:
                return (original, clone, context) -> unsafe.putObject(clone, offset, unsafe.getObject(original, offset));
            case DEFAULT:
                return (original, clone, context) ->
                    unsafe.putObject(clone, offset, context.copy(unsafe.getObject(original, offset)));
            default:
                throw Checks.mustNotHappen();
        }
    }

}
