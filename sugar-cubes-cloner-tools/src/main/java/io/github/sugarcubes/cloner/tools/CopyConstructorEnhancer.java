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
package io.github.sugarcubes.cloner.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.SerialVersionUIDAdder;

import io.github.sugarcubes.cloner.generation.CopyConstructors;

/**
 * Adds a copy constructor to a class file, see {@link CopyConstructors} for its contract.
 *
 * <p>The constructor is synthetic, private in final classes and protected in the others: constructors of subclasses
 * call it. A protected constructor would change the serial version computed for a serializable class without
 * {@code serialVersionUID}, so such a class gets {@code serialVersionUID} with the value computed for the original
 * class.</p>
 *
 * @author Maxim Butov
 */
public final class CopyConstructorEnhancer {

    /**
     * Result of enhancing a class file.
     */
    public enum Status {

        /**
         * The copy constructor is added.
         */
        ENHANCED,

        /**
         * The class already has the copy constructor.
         */
        ALREADY_ENHANCED,

        /**
         * The class does not match the masks or is not a class: interface, enum, annotation, descriptor.
         */
        IGNORED,

        /**
         * The class matches the masks but cannot get a copy constructor, see the problem.
         */
        SKIPPED,

    }

    /**
     * Result of enhancing a class file.
     */
    public static final class Result {

        private final Status status;

        private final byte[] bytes;

        private final String problem;

        Result(Status status, byte[] bytes, String problem) {
            this.status = status;
            this.bytes = bytes;
            this.problem = problem;
        }

        /**
         * Returns the status.
         *
         * @return status
         */
        public Status getStatus() {
            return status;
        }

        /**
         * Returns the enhanced class file.
         *
         * @return class file, the original one unless enhanced
         */
        public byte[] getBytes() {
            return bytes;
        }

        /**
         * Returns the problem of a skipped class.
         *
         * @return problem or {@code null}
         */
        public String getProblem() {
            return problem;
        }

    }

    private static final String COPY_CONSTRUCTORS = CopyConstructors.INTERNAL_NAME;

    private static final String INIT = "<init>";

    private static final String REGISTER_DESCRIPTOR = "(Lio/github/sugarcubes/cloner/CopyContext;Ljava/lang/Object;)V";

    private static final String COPY_DESCRIPTOR = "(Lio/github/sugarcubes/cloner/CopyContext;Ljava/lang/Object;"
        + "Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Object;";

    /**
     * Local variables of the copy constructor.
     */
    private static final int THIS = 0;

    private static final int MARKER = 1;

    private static final int ORIGINAL = 2;

    private static final int CONTEXT = 3;

    /**
     * Offset of the major version in a class file.
     */
    private static final int MAJOR_VERSION_OFFSET = 6;

    /**
     * Class files older than Java 5 cannot load class constants.
     */
    private static final int MIN_VERSION = Opcodes.V1_5;

    private CopyConstructorEnhancer() {
    }

    /**
     * Adds the copy constructor to the class file if the class matches the masks and can get it.
     *
     * @param bytes class file
     * @param hierarchy classes of the build
     * @return result
     */
    public static Result enhance(byte[] bytes, ClassHierarchy hierarchy) {
        ClassInfo info = ClassInfo.read(bytes);
        String name = info.getName();
        if (!hierarchy.isTarget(name) || !ClassHierarchy.isClass(info)) {
            return new Result(Status.IGNORED, bytes, null);
        }
        if (info.hasCopyConstructor()) {
            return new Result(Status.ALREADY_ENHANCED, bytes, null);
        }
        Optional<String> problem = hierarchy.problem(name);
        if (problem.isPresent()) {
            return new Result(Status.SKIPPED, bytes, problem.get());
        }
        ClassReader reader = new ClassReader(bytes);
        if (reader.readUnsignedShort(MAJOR_VERSION_OFFSET) < MIN_VERSION) {
            return new Result(Status.SKIPPED, bytes, "class file older than Java 5");
        }
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        boolean top = ClassHierarchy.OBJECT.equals(info.getSuperName())
            || ClassHierarchy.RECORD.equals(info.getSuperName());
        ClassVisitor visitor = new ConstructorAdder(writer, info, top);
        if (!info.is(Opcodes.ACC_FINAL) && !info.declaresSerialVersionUid()
            && !Boolean.FALSE.equals(hierarchy.isSerializable(name))) {
            // sees the original class only: the constructor is added after it
            visitor = new SerialVersionUIDAdder(Opcodes.ASM9, visitor) {
            };
        }
        reader.accept(visitor, 0);
        return new Result(Status.ENHANCED, writer.toByteArray(), null);
    }

    /**
     * Collects the instance fields and adds the constructor at the end of the class.
     */
    private static final class ConstructorAdder extends ClassVisitor {

        private final ClassInfo info;

        private final boolean top;

        private final List<String[]> fields = new ArrayList<>();

        ConstructorAdder(ClassVisitor next, ClassInfo info, boolean top) {
            super(Opcodes.ASM9, next);
            this.info = info;
            this.top = top;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            if ((access & Opcodes.ACC_STATIC) == 0) {
                fields.add(new String[] {name, descriptor});
            }
            return super.visitField(access, name, descriptor, signature, value);
        }

        @Override
        public void visitEnd() {
            String owner = info.getName();
            int access = (info.is(Opcodes.ACC_FINAL) ? Opcodes.ACC_PRIVATE : Opcodes.ACC_PROTECTED)
                | Opcodes.ACC_SYNTHETIC;
            MethodVisitor code = super.visitMethod(access, INIT, CopyConstructors.descriptor(owner), null, null);
            code.visitCode();
            code.visitVarInsn(Opcodes.ALOAD, THIS);
            if (top) {
                code.visitMethodInsn(Opcodes.INVOKESPECIAL, info.getSuperName(), INIT, "()V", false);
                code.visitVarInsn(Opcodes.ALOAD, CONTEXT);
                code.visitVarInsn(Opcodes.ALOAD, THIS);
                code.visitMethodInsn(Opcodes.INVOKESTATIC, COPY_CONSTRUCTORS, "register", REGISTER_DESCRIPTOR,
                    false);
            }
            else {
                code.visitVarInsn(Opcodes.ALOAD, MARKER);
                code.visitVarInsn(Opcodes.ALOAD, ORIGINAL);
                code.visitVarInsn(Opcodes.ALOAD, CONTEXT);
                code.visitMethodInsn(Opcodes.INVOKESPECIAL, info.getSuperName(), INIT,
                    CopyConstructors.descriptor(info.getSuperName()), false);
            }
            for (String[] field : fields) {
                copyField(code, owner, field[0], field[1]);
            }
            code.visitInsn(Opcodes.RETURN);
            code.visitMaxs(0, 0);
            code.visitEnd();
            super.visitEnd();
        }

        /**
         * {@code this.f = original.f} for primitives,
         * {@code this.f = (T) CopyConstructors.copy(context, original.f, this, Owner.class, "f")} for references.
         */
        private static void copyField(MethodVisitor code, String owner, String name, String descriptor) {
            Type type = Type.getType(descriptor);
            code.visitVarInsn(Opcodes.ALOAD, THIS);
            if (type.getSort() != Type.OBJECT && type.getSort() != Type.ARRAY) {
                code.visitVarInsn(Opcodes.ALOAD, ORIGINAL);
                code.visitFieldInsn(Opcodes.GETFIELD, owner, name, descriptor);
                code.visitFieldInsn(Opcodes.PUTFIELD, owner, name, descriptor);
                return;
            }
            code.visitVarInsn(Opcodes.ALOAD, CONTEXT);
            code.visitVarInsn(Opcodes.ALOAD, ORIGINAL);
            code.visitFieldInsn(Opcodes.GETFIELD, owner, name, descriptor);
            code.visitVarInsn(Opcodes.ALOAD, THIS);
            code.visitLdcInsn(Type.getObjectType(owner));
            code.visitLdcInsn(name);
            code.visitMethodInsn(Opcodes.INVOKESTATIC, COPY_CONSTRUCTORS, "copy", COPY_DESCRIPTOR, false);
            if (!"Ljava/lang/Object;".equals(descriptor)) {
                code.visitTypeInsn(Opcodes.CHECKCAST, type.getInternalName());
            }
            code.visitFieldInsn(Opcodes.PUTFIELD, owner, name, descriptor);
        }

    }

}
