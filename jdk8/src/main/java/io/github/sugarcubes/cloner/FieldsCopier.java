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
package io.github.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

import io.github.sugarcubes.cloner.spi.FieldAdapter;
import io.github.sugarcubes.cloner.spi.FieldsModel;
import io.github.sugarcubes.cloner.spi.InstanceAllocator;
import io.github.sugarcubes.cloner.util.Checks;

/**
 * Copier of objects whose state is their fields. Gets from the low-level {@link FieldsModel} how to allocate an
 * instance and to read and write fields, and decides itself what to copy: applies the field policy, copies primitives,
 * references and clones, subscribes to clones being created right now.
 *
 * <p>The copier of a type copies the fields declared by the type and asks the copier of the super type for the
 * inherited ones.</p>
 *
 * @param <T> type
 * @author Maxim Butov
 */
public class FieldsCopier<T> extends TwoPhaseObjectCopier<T> {

    /**
     * Object allocator of the type, throws {@link ClonerException} if no allocator supports the type.
     */
    private final InstanceAllocator<T> allocator;

    /**
     * Copier of the super type, {@code null} if the super types have no fields to copy.
     */
    private final FieldsCopier<? super T> parent;

    /**
     * Copying of the fields declared by the type.
     */
    private final FieldStep[] steps;

    /**
     * Creates the copier.
     *
     * @param parent copier of the super type
     * @param model low-level model of the type
     * @param policy field copy policy
     * @param shallow shallow mode: references are copied as is
     */
    @SuppressWarnings("unchecked")
    public FieldsCopier(FieldsCopier<?> parent, FieldsModel<T> model, CopyPolicy<Field> policy, boolean shallow) {
        Class<T> type = model.getType();
        // abstract superclasses and classes without suitable allocator fail only if somebody tries to create them
        this.allocator = model.getAllocator().orElseGet(() -> () -> {
            throw new ClonerException(String.format("No instance allocator supports %s.", type.getName()));
        });
        this.parent = (FieldsCopier<? super T>) (parent != null && parent.steps.length == 0 ? parent.parent : parent);
        CopyPolicy<Field> fieldPolicy = shallow ? CopyPolicy.compound(policy, CopyPolicy.original()) : policy;
        this.steps = Arrays.stream(model.getFields())
            .map(field -> FieldStep.of(model, field, fieldPolicy.getAction(field)))
            .filter(Objects::nonNull)
            .toArray(FieldStep[]::new);
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
        for (FieldStep step : steps) {
            step.copy(original, clone, context);
        }
    }

    /**
     * Copying of one field. The kind is chosen once, copying is a single switch, so the field adapter call site sees
     * one adapter class of the toolset.
     */
    private static final class FieldStep {

        private static final int BOOLEAN = 0;
        private static final int BYTE = 1;
        private static final int CHAR = 2;
        private static final int SHORT = 3;
        private static final int INT = 4;
        private static final int LONG = 5;
        private static final int FLOAT = 6;
        private static final int DOUBLE = 7;
        private static final int NULL = 8;
        private static final int ORIGINAL = 9;
        private static final int CLONE = 10;

        private final FieldAdapter adapter;

        private final int kind;

        private FieldStep(FieldAdapter adapter, int kind) {
            this.adapter = adapter;
            this.kind = kind;
        }

        /**
         * Creates the step for the field.
         *
         * @return step or {@code null} if the field is skipped
         */
        static FieldStep of(FieldsModel<?> model, Field field, CopyAction action) {
            switch (action) {
                case SKIP:
                    return null;
                case NULL:
                    Checks.illegalArg(field.getType().isPrimitive(), "Cannot apply action NULL for primitive field %s.",
                        field);
                    return new FieldStep(model.adapter(field), NULL);
                case ORIGINAL:
                case DEFAULT:
                    if (field.getType().isPrimitive()) {
                        return new FieldStep(model.adapter(field), primitiveKind(field.getType()));
                    }
                    return new FieldStep(model.adapter(field), action == CopyAction.ORIGINAL ? ORIGINAL : CLONE);
                default:
                    throw Checks.mustNotHappen();
            }
        }

        private static int primitiveKind(Class<?> type) {
            switch (type.getName()) {
                case "boolean":
                    return BOOLEAN;
                case "byte":
                    return BYTE;
                case "char":
                    return CHAR;
                case "short":
                    return SHORT;
                case "int":
                    return INT;
                case "long":
                    return LONG;
                case "float":
                    return FLOAT;
                case "double":
                    return DOUBLE;
                default:
                    throw Checks.mustNotHappen();
            }
        }

        @SuppressWarnings("checkstyle:CyclomaticComplexity")
        void copy(Object original, Object clone, CopyContext context) throws Exception {
            FieldAdapter adapter = this.adapter;
            switch (kind) {
                case BOOLEAN:
                    adapter.setBoolean(clone, adapter.getBoolean(original));
                    break;
                case BYTE:
                    adapter.setByte(clone, adapter.getByte(original));
                    break;
                case CHAR:
                    adapter.setChar(clone, adapter.getChar(original));
                    break;
                case SHORT:
                    adapter.setShort(clone, adapter.getShort(original));
                    break;
                case INT:
                    adapter.setInt(clone, adapter.getInt(original));
                    break;
                case LONG:
                    adapter.setLong(clone, adapter.getLong(original));
                    break;
                case FLOAT:
                    adapter.setFloat(clone, adapter.getFloat(original));
                    break;
                case DOUBLE:
                    adapter.setDouble(clone, adapter.getDouble(original));
                    break;
                case NULL:
                    adapter.setObject(clone, null);
                    break;
                case ORIGINAL:
                    adapter.setObject(clone, adapter.getObject(original));
                    break;
                case CLONE:
                    copyReference(original, clone, context);
                    break;
                default:
                    throw Checks.mustNotHappen();
            }
        }

        private void copyReference(Object original, Object clone, CopyContext context) throws Exception {
            Object value = adapter.getObject(original);
            Object copy = context.copyNow(value);
            if (copy != CopyContext.PENDING) {
                adapter.setObject(clone, copy);
            }
            else {
                context.subscribe(value, later -> adapter.setObject(clone, later));
            }
        }

    }

}
