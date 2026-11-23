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

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassHierarchyResolver;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.instruction.SwitchCase;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;

import io.github.sugarcubes.cloner.generation.BytecodeBackend;
import io.github.sugarcubes.cloner.generation.CodeEmitter;
import io.github.sugarcubes.cloner.generation.CodeLabel;
import io.github.sugarcubes.cloner.generation.MethodSpec;

/**
 * Backend on the ClassFile API of JDK 24+.
 *
 * @author Maxim Butov
 */
final class ClassFileBytecodeBackend implements BytecodeBackend {

    /**
     * Stack maps of the template never merge different classes, so unknown classes need not be loaded: they are
     * taken for direct subclasses of {@link Object}.
     */
    private static final ClassFile CLASS_FILE = ClassFile.of(ClassFile.ClassHierarchyResolverOption.of(
        ClassHierarchyResolver.defaultResolver().orElse(ClassFileBytecodeBackend::objectSubclass)));

    private static ClassHierarchyResolver.ClassHierarchyInfo objectSubclass(ClassDesc type) {
        return ClassHierarchyResolver.ClassHierarchyInfo.ofClass(ConstantDescs.CD_Object);
    }

    @Override
    public byte[] write(String name, String superName, List<MethodSpec> methods) {
        return CLASS_FILE.build(ClassDesc.ofInternalName(name), builder -> {
            builder.withVersion(CLASS_VERSION, 0);
            builder.withFlags(ClassFile.ACC_FINAL | ClassFile.ACC_SUPER | ClassFile.ACC_SYNTHETIC);
            builder.withSuperclass(ClassDesc.ofInternalName(superName));
            for (MethodSpec method : methods) {
                builder.withMethodBody(method.getName(), MethodTypeDesc.ofDescriptor(method.getDescriptor()),
                    method.getAccess(), code -> method.emit(new Emitter(code)));
            }
        });
    }

    private static ClassDesc type(String type) {
        // arrays are given by descriptors
        return type.startsWith("[") ? ClassDesc.ofDescriptor(type) : ClassDesc.ofInternalName(type);
    }

    /**
     * Label of the ClassFile API.
     */
    private record ClassFileLabel(Label label) implements CodeLabel {

    }

    /**
     * Emitter writing to a code builder.
     */
    private record Emitter(CodeBuilder code) implements CodeEmitter {

        @Override
        public void loadReference(int slot) {
            code.aload(slot);
        }

        @Override
        public void loadInt(int slot) {
            code.iload(slot);
        }

        @Override
        public void pushNull() {
            code.aconst_null();
        }

        @Override
        public void pushInt(int value) {
            code.loadConstant(value);
        }

        @Override
        public void checkCast(String type) {
            code.checkcast(type(type));
        }

        @Override
        public void getField(String owner, String name, String descriptor) {
            code.getfield(ClassDesc.ofInternalName(owner), name, ClassDesc.ofDescriptor(descriptor));
        }

        @Override
        public void putField(String owner, String name, String descriptor) {
            code.putfield(ClassDesc.ofInternalName(owner), name, ClassDesc.ofDescriptor(descriptor));
        }

        @Override
        public void invokeSpecial(String owner, String name, String descriptor) {
            code.invokespecial(ClassDesc.ofInternalName(owner), name, MethodTypeDesc.ofDescriptor(descriptor));
        }

        @Override
        public void invokeVirtual(String owner, String name, String descriptor) {
            code.invokevirtual(ClassDesc.ofInternalName(owner), name, MethodTypeDesc.ofDescriptor(descriptor));
        }

        @Override
        public void returnVoid() {
            code.return_();
        }

        @Override
        public CodeLabel newLabel() {
            return new ClassFileLabel(code.newLabel());
        }

        @Override
        public void bind(CodeLabel label) {
            code.labelBinding(((ClassFileLabel) label).label());
        }

        @Override
        public void tableSwitch(int low, CodeLabel defaultLabel, CodeLabel... labels) {
            List<SwitchCase> cases = new ArrayList<>();
            for (int i = 0; i < labels.length; i++) {
                cases.add(SwitchCase.of(low + i, ((ClassFileLabel) labels[i]).label()));
            }
            code.tableswitch(low, low + labels.length - 1, ((ClassFileLabel) defaultLabel).label(), cases);
        }

    }

}
