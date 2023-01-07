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
package org.sugarcubes.cloner;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;

/**
 * Field copiers factory which uses {@link VarHandle} for getting/setting field values.
 *
 * @author Maxim Butov
 */
public final class VarHandleFieldCopierFactory implements FieldCopierFactory {

    /**
     * Trusted {@link MethodHandles.Lookup} instance.
     */
    private static final MethodHandles.Lookup LOOKUP = MethodHandlesLookupUtils.getLookup();

    @Override
    public FieldCopier getFieldCopier(Field field, CopyAction action) {
        if (action == CopyAction.SKIP) {
            return FieldCopier.NOOP;
        }
        Checks.illegalArg(field.getType().isPrimitive() && action == CopyAction.NULL,
            "Cannot apply action NULL for primitive field %s.", field);
        VarHandle handle = ClonerExceptionUtils.replaceException(() -> LOOKUP.unreflectVarHandle(field));
        switch (action) {
            case NULL:
                return (original, clone, context) -> handle.set(clone, null);
            case ORIGINAL:
                return (original, clone, context) -> handle.set(clone, handle.get(original));
            case DEFAULT:
                return (original, clone, context) -> handle.set(clone, context.copy(handle.get(original)));
            default:
                throw Checks.neverHappens();
        }
    }

}
