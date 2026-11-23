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

import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * Backend on the ASM library, which must be on the classpath; loading this class needs ASM, so its availability is
 * checked by {@link CopierGenerator} without touching this class.
 *
 * @author Maxim Butov
 */
public final class AsmBytecodeBackend implements BytecodeBackend {

    @Override
    public byte[] write(String name, String superName, List<MethodSpec> methods) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                // frames of the template never merge different classes, so classes are not loaded to find out
                return "java/lang/Object";
            }
        };
        writer.visit(CLASS_VERSION, Opcodes.ACC_FINAL | Opcodes.ACC_SUPER | Opcodes.ACC_SYNTHETIC, name, null,
            superName, null);
        for (MethodSpec method : methods) {
            MethodVisitor visitor = writer.visitMethod(method.getAccess(), method.getName(), method.getDescriptor(), null,
                null);
            visitor.visitCode();
            method.emit(new Emitter(visitor));
            visitor.visitMaxs(0, 0);
            visitor.visitEnd();
        }
        writer.visitEnd();
        return writer.toByteArray();
    }

    /**
     * Label of ASM.
     */
    private static final class AsmLabel implements CodeLabel {

        final Label label = new Label();

    }

    /**
     * Emitter writing to an ASM method visitor.
     */
    private static final class Emitter implements CodeEmitter {

        /**
         * The largest constant of the {@code iconst_<n>} instructions.
         */
        private static final int MAX_ICONST = 5;

        private final MethodVisitor visitor;

        Emitter(MethodVisitor visitor) {
            this.visitor = visitor;
        }

        @Override
        public void loadReference(int slot) {
            visitor.visitVarInsn(Opcodes.ALOAD, slot);
        }

        @Override
        public void loadInt(int slot) {
            visitor.visitVarInsn(Opcodes.ILOAD, slot);
        }

        @Override
        public void pushNull() {
            visitor.visitInsn(Opcodes.ACONST_NULL);
        }

        @Override
        public void pushInt(int value) {
            if (value >= -1 && value <= MAX_ICONST) {
                visitor.visitInsn(Opcodes.ICONST_0 + value);
            }
            else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                visitor.visitIntInsn(Opcodes.BIPUSH, value);
            }
            else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                visitor.visitIntInsn(Opcodes.SIPUSH, value);
            }
            else {
                visitor.visitLdcInsn(value);
            }
        }

        @Override
        public void checkCast(String type) {
            visitor.visitTypeInsn(Opcodes.CHECKCAST, type);
        }

        @Override
        public void getField(String owner, String name, String descriptor) {
            visitor.visitFieldInsn(Opcodes.GETFIELD, owner, name, descriptor);
        }

        @Override
        public void putField(String owner, String name, String descriptor) {
            visitor.visitFieldInsn(Opcodes.PUTFIELD, owner, name, descriptor);
        }

        @Override
        public void invokeSpecial(String owner, String name, String descriptor) {
            visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, owner, name, descriptor, false);
        }

        @Override
        public void invokeVirtual(String owner, String name, String descriptor) {
            visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, name, descriptor, false);
        }

        @Override
        public void returnVoid() {
            visitor.visitInsn(Opcodes.RETURN);
        }

        @Override
        public CodeLabel newLabel() {
            return new AsmLabel();
        }

        @Override
        public void bind(CodeLabel label) {
            visitor.visitLabel(((AsmLabel) label).label);
        }

        @Override
        public void tableSwitch(int low, CodeLabel defaultLabel, CodeLabel... labels) {
            Label[] targets = new Label[labels.length];
            for (int i = 0; i < labels.length; i++) {
                targets[i] = ((AsmLabel) labels[i]).label;
            }
            visitor.visitTableSwitchInsn(low, low + labels.length - 1, ((AsmLabel) defaultLabel).label, targets);
        }

    }

}
