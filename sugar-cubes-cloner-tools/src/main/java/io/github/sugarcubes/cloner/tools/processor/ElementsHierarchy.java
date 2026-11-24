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
package io.github.sugarcubes.cloner.tools.processor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.objectweb.asm.Opcodes;

import io.github.sugarcubes.cloner.generation.ClassNameFilter;
import io.github.sugarcubes.cloner.tools.ClassFiles;
import io.github.sugarcubes.cloner.tools.ClassHierarchy;
import io.github.sugarcubes.cloner.tools.ClassInfo;

/**
 * Classes as the processor sees them: types of the compilation and of the classpath by the language model; whether a
 * class of the classpath has a copy constructor is read from its class file, javac does not show synthetic members.
 *
 * @author Maxim Butov
 */
final class ElementsHierarchy extends ClassHierarchy {

    private final Elements elements;

    private final Filer filer;

    private final Map<String, TypeElement> local = new HashMap<>();

    private final Map<String, ClassInfo> cache = new HashMap<>();

    ElementsHierarchy(ClassNameFilter targets, Elements elements, Filer filer) {
        super(targets);
        this.elements = elements;
        this.filer = filer;
    }

    /**
     * Adds a type of the compilation and its nested types.
     *
     * @param type type
     */
    void addLocal(TypeElement type) {
        local.put(internalName(type), type);
        for (TypeElement nested : ElementFilter.typesIn(type.getEnclosedElements())) {
            addLocal(nested);
        }
    }

    /**
     * Returns the type of the compilation.
     *
     * @param name internal name
     * @return type or {@code null}
     */
    TypeElement getLocal(String name) {
        return local.get(name);
    }

    /**
     * Returns the internal name of the type.
     *
     * @param type type
     * @return internal name
     */
    String internalName(TypeElement type) {
        return elements.getBinaryName(type).toString().replace('.', '/');
    }

    /**
     * Returns the internal names of the types of the compilation.
     *
     * @return internal names
     */
    Set<String> localNames() {
        return Collections.unmodifiableSet(local.keySet());
    }

    @Override
    protected boolean isLocal(String name) {
        return local.containsKey(name);
    }

    @Override
    protected ClassInfo find(String name) {
        if (!cache.containsKey(name)) {
            cache.put(name, read(name));
        }
        return cache.get(name);
    }

    private ClassInfo read(String name) {
        TypeElement type = local.get(name);
        if (type != null) {
            return info(name, type, false);
        }
        ClassInfo compiled = readClassFile(name);
        if (compiled != null) {
            return compiled;
        }
        // e.g. JDK classes, not on the classpath
        type = elements.getTypeElement(name.replace('/', '.').replace('$', '.'));
        return type != null ? info(name, type, false) : null;
    }

    private ClassInfo readClassFile(String name) {
        int slash = name.lastIndexOf('/');
        String packageName = slash >= 0 ? name.substring(0, slash).replace('/', '.') : "";
        try {
            FileObject file = filer.getResource(StandardLocation.CLASS_PATH, packageName,
                name.substring(slash + 1) + ".class");
            try (InputStream in = file.openInputStream()) {
                return ClassInfo.read(ClassFiles.readAll(in));
            }
        }
        catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Access flags of the class which the build tools check.
     */
    private static int access(TypeElement type) {
        int access = 0;
        ElementKind kind = type.getKind();
        if (kind == ElementKind.INTERFACE) {
            access |= Opcodes.ACC_INTERFACE;
        }
        else if (kind == ElementKind.ANNOTATION_TYPE) {
            access |= Opcodes.ACC_INTERFACE | Opcodes.ACC_ANNOTATION;
        }
        else if (kind == ElementKind.ENUM) {
            access |= Opcodes.ACC_ENUM;
        }
        // records are final, ElementKind.RECORD appeared in JDK 16
        if (type.getModifiers().contains(Modifier.FINAL) || "RECORD".equals(kind.name())) {
            access |= Opcodes.ACC_FINAL;
        }
        return access;
    }

    private ClassInfo info(String name, TypeElement type, boolean copyConstructor) {
        int access = access(type);
        String superName = null;
        TypeMirror superclass = type.getSuperclass();
        if (superclass.getKind() == TypeKind.DECLARED) {
            superName = internalName((TypeElement) ((DeclaredType) superclass).asElement());
        }
        List<String> interfaces = new ArrayList<>();
        for (TypeMirror mirror : type.getInterfaces()) {
            if (mirror.getKind() == TypeKind.DECLARED) {
                interfaces.add(internalName((TypeElement) ((DeclaredType) mirror).asElement()));
            }
        }
        boolean serialVersionUid = false;
        for (Element field : ElementFilter.fieldsIn(type.getEnclosedElements())) {
            serialVersionUid |= field.getSimpleName().contentEquals("serialVersionUID")
                && field.getModifiers().contains(Modifier.STATIC);
        }
        return new ClassInfo(name, access, superName, interfaces, copyConstructor, serialVersionUid);
    }

}
