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

/**
 * Base field copier factory.
 *
 * @author Maxim Butov
 */
public abstract class AbstractFieldCopierFactory implements FieldCopierFactory {

    @Override
    public FieldCopier getFieldCopier(Field field, CopyAction action) {
        if (action == CopyAction.SKIP) {
            return FieldCopier.NOOP;
        }
        if (field.getType().isPrimitive()) {
            Checks.illegalArg(action == CopyAction.NULL, "Cannot apply action NULL for primitive field %s.", field);
            return getPrimitiveFieldCopier(field);
        }
        return getObjectFieldCopier(field, action);
    }

    /**
     * Returns field copier for the primitive field.
     *
     * @param field field
     * @return field copier
     */
    protected abstract FieldCopier getPrimitiveFieldCopier(Field field);

    /**
     * Returns specific field copier for the object field and the action.
     *
     * @param field field
     * @param action copying action
     * @return field copy action
     */
    protected abstract FieldCopier getObjectFieldCopier(Field field, CopyAction action);

}
