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
package io.github.sugarcubes.cloner.tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.sugarcubes.cloner.generation.CopyConstructors;

/**
 * What the build tools need to know about a class: its kind, superclass, interfaces and whether it already has a copy
 * constructor.
 *
 * @author Maxim Butov
 */
public final class ClassInfo {

    /**
     * Name of the field which overrides the computed serial version.
     */
    static final String SERIAL_VERSION_UID = "serialVersionUID";

    private final String name;

    private final int access;

    private final String superName;

    private final List<String> interfaces;

    private final boolean copyConstructor;

    private final boolean serialVersionUid;

    /**
     * Constructor.
     *
     * @param name internal name
     * @param access access flags of the class
     * @param superName internal name of the superclass, {@code null} for {@code Object}
     * @param interfaces internal names of the interfaces
     * @param copyConstructor whether the class has a copy constructor
     * @param serialVersionUid whether the class declares {@code serialVersionUID}
     */
    public ClassInfo(String name, int access, String superName, List<String> interfaces, boolean copyConstructor,
        boolean serialVersionUid) {
        this.name = name;
        this.access = access;
        this.superName = superName;
        this.interfaces = Collections.unmodifiableList(interfaces);
        this.copyConstructor = copyConstructor;
        this.serialVersionUid = serialVersionUid;
    }

    /**
     * Reads the information from a class file.
     *
     * @param bytes class file
     * @return class information
     */
    public static ClassInfo read(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        String name = reader.getClassName();
        String descriptor = CopyConstructors.descriptor(name);
        boolean[] members = new boolean[2];
        reader.accept(new ClassVisitor(Opcodes.ASM9) {

            @Override
            public FieldVisitor visitField(int access, String field, String fieldDescriptor, String signature,
                Object value) {
                if (SERIAL_VERSION_UID.equals(field) && (access & Opcodes.ACC_STATIC) != 0) {
                    members[1] = true;
                }
                return null;
            }

            @Override
            public MethodVisitor visitMethod(int access, String method, String methodDescriptor, String signature,
                String[] exceptions) {
                if ("<init>".equals(method) && descriptor.equals(methodDescriptor)) {
                    members[0] = true;
                }
                return null;
            }

        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return new ClassInfo(name, reader.getAccess(), reader.getSuperName(), Arrays.asList(reader.getInterfaces()),
            members[0], members[1]);
    }

    /**
     * Returns the internal name.
     *
     * @return internal name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the access flags.
     *
     * @return access flags
     */
    public int getAccess() {
        return access;
    }

    /**
     * Returns the internal name of the superclass.
     *
     * @return internal name or {@code null} for {@code Object}
     */
    public String getSuperName() {
        return superName;
    }

    /**
     * Returns the internal names of the interfaces.
     *
     * @return internal names
     */
    public List<String> getInterfaces() {
        return interfaces;
    }

    /**
     * Tells whether the class has a copy constructor.
     *
     * @return {@code true} if it has
     */
    public boolean hasCopyConstructor() {
        return copyConstructor;
    }

    /**
     * Tells whether the class declares {@code serialVersionUID}.
     *
     * @return {@code true} if it declares
     */
    public boolean declaresSerialVersionUid() {
        return serialVersionUid;
    }

    /**
     * Tells whether the flag is set.
     *
     * @param flag access flag
     * @return {@code true} if set
     */
    public boolean is(int flag) {
        return (access & flag) != 0;
    }

}
