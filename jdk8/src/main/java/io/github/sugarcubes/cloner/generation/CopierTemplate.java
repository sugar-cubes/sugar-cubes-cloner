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
package io.github.sugarcubes.cloner.generation;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.CopyAction;
import io.github.sugarcubes.cloner.CopyContext;
import io.github.sugarcubes.cloner.CopyPolicy;
import io.github.sugarcubes.cloner.TwoPhaseObjectCopier;
import io.github.sugarcubes.cloner.spi.FieldAdapter;
import io.github.sugarcubes.cloner.spi.FieldsModel;
import io.github.sugarcubes.cloner.spi.InstanceAllocator;
import io.github.sugarcubes.cloner.util.Checks;

/**
 * Copier class of a type, a subclass of {@link GeneratedFieldsCopier}: the same copying as
 * {@code engine.FieldsCopier}, with the field policy applied when the class is generated.
 *
 * <p>Local variables of {@code copyFields}: 0 - the copier, 1 - original, 2 - clone, 3 - copy context. Local
 * variables of {@code setField}: 0 - the copier, 1 - field index, 2 - target, 3 - value.</p>
 *
 * @author Maxim Butov
 */
final class CopierTemplate {

    /**
     * Suffix of the generated class name.
     */
    static final String SUFFIX = "$$Copier";

    private static final String SUPER = Descriptors.internalName(GeneratedFieldsCopier.class);

    private static final String CONSTRUCTOR = "<init>";

    private static final String SET_FINAL = "setFinal";

    private static final String METHOD_HANDLE = "java/lang/invoke/MethodHandle";

    private static final String METHOD_HANDLE_DESCRIPTOR = Descriptors.descriptor(MethodHandle.class);

    private static final String METHOD_HANDLES = "java/lang/invoke/MethodHandles";

    private static final String SETTER = "$setter";

    /**
     * Synthetic access flag, not in {@link Modifier}.
     */
    private static final int SYNTHETIC = 0x1000;

    private static final int ORIGINAL = 1;
    private static final int CLONE = 2;
    private static final int CONTEXT = 3;
    private static final int INDEX = 1;
    private static final int TARGET = 2;
    private static final int VALUE = 3;

    private final Class<?> type;

    private final String owner;

    private final List<FieldPlan> fields = new ArrayList<>();

    private final List<FieldAdapter> adapters = new ArrayList<>();

    private final List<FieldPlan> holes = new ArrayList<>();

    /**
     * Final fields written by setters: method handles in static final fields, read from the class data.
     */
    private final List<Field> setters = new ArrayList<>();

    /**
     * Actions of the declared fields in their order, the class file depends on nothing else of the type.
     */
    private final StringBuilder signature = new StringBuilder();

    /**
     * Plans the copying of the fields declared by the type.
     *
     * @param model fields model of the type
     * @param policy field policy
     * @param finalSetters whether final fields with values known at once are written by method handles from the class
     *     data instead of field adapters
     */
    CopierTemplate(FieldsModel<?> model, CopyPolicy<Field> policy, boolean finalSetters) {
        this.type = model.getType();
        this.owner = Descriptors.internalName(type);
        for (Field field : model.getFields()) {
            CopyAction action = policy.getAction(field);
            signature.append(action.name().charAt(0));
            if (action == CopyAction.SKIP) {
                continue;
            }
            Checks.illegalArg(action == CopyAction.NULL && field.getType().isPrimitive(),
                "Cannot apply action NULL for primitive field %s.", field);
            FieldPlan plan = new FieldPlan(field, action);
            if (plan.isFinal && finalSetters && !plan.isHole()) {
                plan.setter = setters.size();
                setters.add(field);
            }
            else if (plan.isFinal) {
                plan.index = adapters.size();
                adapters.add(model.adapter(field).orElseThrow(() -> new ClonerException(String.format("Cannot "
                    + "access field %s: open its package to the cloner with --add-opens JVM option or use a toolset "
                    + "with Unsafe field access.", field))));
            }
            else if (plan.isHole()) {
                plan.index = holes.size();
                holes.add(plan);
            }
            fields.add(plan);
        }
        signature.append(finalSetters ? '+' : '-');
    }

    /**
     * Returns the final fields written by setters, in the order of the class data.
     *
     * @return fields
     */
    List<Field> setterFields() {
        return setters;
    }

    /**
     * Returns the actions of the declared fields: copiers of the type with the same signature are the same class.
     *
     * @return one letter of the action per field
     */
    String signature() {
        return signature.toString();
    }

    /**
     * Tells whether the type has fields to copy.
     *
     * @return {@code true} if there is nothing to copy
     */
    boolean isEmpty() {
        return fields.isEmpty();
    }

    /**
     * Returns the internal name of the generated class, in the package of the type.
     *
     * @return internal name
     */
    String className() {
        return owner + SUFFIX;
    }

    /**
     * Returns the adapters of the final fields for the constructor of the generated class.
     *
     * @return adapters
     */
    FieldAdapter[] adapters() {
        return adapters.toArray(new FieldAdapter[0]);
    }

    /**
     * Writes the class.
     *
     * @param backend bytecode backend
     * @param version class file version
     * @return class file
     */
    byte[] write(BytecodeBackend backend, int version) {
        String copyFields = Descriptors.method(void.class, Object.class, Object.class, CopyContext.class);
        String setField = Descriptors.method(void.class, int.class, Object.class, Object.class);
        List<MethodSpec> methods = new ArrayList<>(Arrays.asList(
            new MethodSpec(Modifier.PUBLIC, CONSTRUCTOR, constructorDescriptor(), this::constructor),
            new MethodSpec(Modifier.PROTECTED, "copyFields", copyFields, this::copyFields),
            new MethodSpec(Modifier.PROTECTED, "setField", setField, this::setField)
        ));
        List<FieldSpec> staticFields = new ArrayList<>();
        for (int i = 0; i < setters.size(); i++) {
            staticFields.add(new FieldSpec(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL | SYNTHETIC, SETTER + i,
                METHOD_HANDLE_DESCRIPTOR));
        }
        if (!setters.isEmpty()) {
            methods.add(new MethodSpec(Modifier.STATIC, "<clinit>", "()V", this::staticInitializer));
        }
        return backend.write(version, className(), SUPER, staticFields, methods);
    }

    /**
     * {@code $setter<i> = MethodHandles.classDataAt(MethodHandles.lookup(), "_", MethodHandle.class, i)}.
     */
    private void staticInitializer(CodeEmitter code) {
        String classDataAt = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;I)"
            + "Ljava/lang/Object;";
        for (int i = 0; i < setters.size(); i++) {
            code.invokeStatic(METHOD_HANDLES, "lookup", "()Ljava/lang/invoke/MethodHandles$Lookup;");
            code.loadString("_");
            code.loadClass(METHOD_HANDLE);
            code.pushInt(i);
            code.invokeStatic(METHOD_HANDLES, "classDataAt", classDataAt);
            code.checkCast(METHOD_HANDLE);
            code.putStatic(className(), SETTER + i, METHOD_HANDLE_DESCRIPTOR);
        }
        code.returnVoid();
    }

    /**
     * Descriptor of the constructor of the generated class and of {@link GeneratedFieldsCopier}.
     *
     * @return descriptor
     */
    static String constructorDescriptor() {
        return Descriptors.method(void.class, InstanceAllocator.class, TwoPhaseObjectCopier.class, FieldAdapter[].class);
    }

    private void constructor(CodeEmitter code) {
        code.loadReference(0);
        code.loadReference(1);
        code.loadReference(2);
        code.loadReference(VALUE);
        code.invokeSpecial(SUPER, CONSTRUCTOR, constructorDescriptor());
        code.returnVoid();
    }

    private void copyFields(CodeEmitter code) {
        for (FieldPlan field : fields) {
            if (field.isFinal) {
                copyFinal(code, field);
            }
            else {
                copyDirect(code, field);
            }
        }
        code.returnVoid();
    }

    /**
     * Non-final field: {@code clone.f = original.f}, {@code null} or {@code copyReference(...)}.
     */
    private void copyDirect(CodeEmitter code, FieldPlan field) {
        loadTyped(code, CLONE);
        if (field.action == CopyAction.NULL) {
            code.pushNull();
        }
        else if (field.isHole()) {
            code.loadReference(0);
            code.loadReference(CONTEXT);
            readOriginal(code, field);
            code.loadReference(CLONE);
            code.pushInt(field.index);
            code.invokeVirtual(SUPER, "copyReference",
                Descriptors.method(Object.class, CopyContext.class, Object.class, Object.class, int.class));
            code.checkCast(Descriptors.castType(field.field.getType()));
        }
        else {
            readOriginal(code, field);
        }
        code.putField(owner, field.field.getName(), field.descriptor);
    }

    /**
     * Final field: written by its setter, {@code $setter<i>.invokeExact((Type) clone, value)}, or by the adapter with
     * the given index.
     */
    private void copyFinal(CodeEmitter code, FieldPlan field) {
        if (field.setter >= 0) {
            code.getStatic(className(), SETTER + field.setter, METHOD_HANDLE_DESCRIPTOR);
            loadTyped(code, CLONE);
            if (field.action == CopyAction.NULL) {
                code.pushNull();
            }
            else {
                readOriginal(code, field);
            }
            code.invokeVirtual(METHOD_HANDLE, "invokeExact", Descriptors.method(void.class, type, field.field.getType()));
            return;
        }
        code.loadReference(0);
        if (field.isHole()) {
            code.loadReference(CONTEXT);
            readOriginal(code, field);
            code.loadReference(CLONE);
            code.pushInt(field.index);
            code.invokeVirtual(SUPER, "copyFinalReference",
                Descriptors.method(void.class, CopyContext.class, Object.class, Object.class, int.class));
            return;
        }
        Class<?> valueType = field.field.getType().isPrimitive() ? field.field.getType() : Object.class;
        code.pushInt(field.index);
        code.loadReference(CLONE);
        if (field.action == CopyAction.NULL) {
            code.pushNull();
        }
        else {
            readOriginal(code, field);
        }
        code.invokeVirtual(SUPER, SET_FINAL + setterSuffix(valueType),
            Descriptors.method(void.class, int.class, Object.class, valueType));
    }

    private void readOriginal(CodeEmitter code, FieldPlan field) {
        loadTyped(code, ORIGINAL);
        code.getField(owner, field.field.getName(), field.descriptor);
    }

    private void loadTyped(CodeEmitter code, int slot) {
        code.loadReference(slot);
        code.checkCast(owner);
    }

    /**
     * {@code switch (index)} setting the non-final reference fields filled later.
     */
    private void setField(CodeEmitter code) {
        if (!holes.isEmpty()) {
            CodeLabel end = code.newLabel();
            CodeLabel[] labels = new CodeLabel[holes.size()];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = code.newLabel();
            }
            code.loadInt(INDEX);
            code.tableSwitch(0, end, labels);
            for (int i = 0; i < labels.length; i++) {
                FieldPlan field = holes.get(i);
                code.bind(labels[i]);
                loadTyped(code, TARGET);
                code.loadReference(VALUE);
                code.checkCast(Descriptors.castType(field.field.getType()));
                code.putField(owner, field.field.getName(), field.descriptor);
                code.returnVoid();
            }
            code.bind(end);
        }
        code.returnVoid();
    }

    private static String setterSuffix(Class<?> valueType) {
        String name = valueType == Object.class ? "Object" : valueType.getName();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Copying of one field.
     */
    private static final class FieldPlan {

        final Field field;

        final CopyAction action;

        final String descriptor;

        final boolean isFinal;

        /**
         * Index of the adapter of a final field or of the hole of a non-final one.
         */
        int index;

        /**
         * Index of the setter of a final field, {@code -1} if it is written by an adapter.
         */
        int setter = -1;

        FieldPlan(Field field, CopyAction action) {
            this.field = field;
            this.action = action;
            this.descriptor = Descriptors.descriptor(field.getType());
            this.isFinal = Modifier.isFinal(field.getModifiers());
        }

        /**
         * Tells whether the field gets the clone of its value, maybe later.
         */
        boolean isHole() {
            return action == CopyAction.DEFAULT && !field.getType().isPrimitive();
        }

    }

}
