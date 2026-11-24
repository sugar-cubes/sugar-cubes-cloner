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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.objectweb.asm.Opcodes;

import io.github.sugarcubes.cloner.generation.ClassNameFilter;
import io.github.sugarcubes.cloner.generation.GenerationTargets;

/**
 * Classes of a build and of its classpath, as the build tools see them: which classes get copy constructors in this
 * build and which superclasses have them.
 *
 * <p>A class gets a copy constructor if it is compiled in this build, matches the masks, is a class (not an interface,
 * an enum, an annotation, a module or package descriptor), and its superclass is {@code Object}, {@code Record} or a
 * class with a copy constructor: the constructor calls the copy constructor of the superclass.</p>
 *
 * @author Maxim Butov
 */
public abstract class ClassHierarchy {

    /**
     * Internal name of {@code java.lang.Object}.
     */
    public static final String OBJECT = "java/lang/Object";

    /**
     * Internal name of {@code java.lang.Record}.
     */
    public static final String RECORD = "java/lang/Record";

    private static final String SERIALIZABLE = "java/io/Serializable";

    private static final int NOT_A_CLASS = Opcodes.ACC_INTERFACE | Opcodes.ACC_ENUM | Opcodes.ACC_ANNOTATION
        | Opcodes.ACC_MODULE;

    private final ClassNameFilter targets;

    private final Map<String, Optional<String>> problems = new HashMap<>();

    /**
     * Constructor.
     *
     * @param targets classes which get copy constructors
     */
    protected ClassHierarchy(ClassNameFilter targets) {
        this.targets = targets;
    }

    /**
     * Finds the class.
     *
     * @param name internal name
     * @return class information or {@code null} if the class is not found
     */
    protected abstract ClassInfo find(String name);

    /**
     * Tells whether the class is compiled in this build.
     *
     * @param name internal name
     * @return {@code true} for classes of this build
     */
    protected abstract boolean isLocal(String name);

    /**
     * Tells whether the class matches the masks.
     *
     * @param name internal name
     * @return {@code true} if it matches
     */
    public boolean isTarget(String name) {
        return targets.matches(name.replace('/', '.'));
    }

    /**
     * Tells whether the class is a class, the only kind which gets copy constructors.
     *
     * @param info class
     * @return {@code false} for interfaces, enums, annotations, module and package descriptors
     */
    public static boolean isClass(ClassInfo info) {
        return !info.is(NOT_A_CLASS) && !info.getName().endsWith("/package-info");
    }

    /**
     * Explains why a class of this build which matches the masks and is a class cannot get a copy constructor.
     *
     * @param name internal name
     * @return problem or empty if the class gets a copy constructor, is not a target or not a class
     */
    public Optional<String> targetProblem(String name) {
        ClassInfo info = find(name);
        if (info == null || !isTarget(name) || !isClass(info) || info.hasCopyConstructor()) {
            return Optional.empty();
        }
        return problem(name);
    }

    /**
     * Explains why a class of this build which matches the masks cannot get a copy constructor.
     *
     * @param name internal name
     * @return problem or empty if the class gets a copy constructor
     */
    public Optional<String> problem(String name) {
        Optional<String> known = problems.get(name);
        if (known == null) {
            // a cycle is impossible: superclasses form a tree
            known = findProblem(name);
            problems.put(name, known);
        }
        return known;
    }

    private Optional<String> findProblem(String name) {
        ClassInfo info = find(name);
        if (info == null) {
            return Optional.of("not found");
        }
        if (!isClass(info)) {
            return Optional.of("not a class");
        }
        String superName = info.getSuperName();
        if (superName == null || OBJECT.equals(superName) || RECORD.equals(superName)
            || hasCopyConstructor(superName)) {
            return Optional.empty();
        }
        return Optional.of(String.format("superclass %s has no copy constructor; list it in %s or exclude this "
            + "class", superName.replace('/', '.'), GenerationTargets.RESOURCE));
    }

    /**
     * Tells whether the class has a copy constructor or gets it in this build.
     *
     * @param name internal name
     * @return {@code true} if instances of subclasses can call it
     */
    public boolean hasCopyConstructor(String name) {
        if (isLocal(name)) {
            return isTarget(name) && !problem(name).isPresent();
        }
        ClassInfo info = find(name);
        return info != null && info.hasCopyConstructor();
    }

    /**
     * Tells whether instances of the class are serializable.
     *
     * @param name internal name
     * @return {@code true} or {@code false}, {@code null} if a supertype is not found
     */
    public Boolean isSerializable(String name) {
        return isSerializable(name, new HashSet<>());
    }

    private Boolean isSerializable(String name, Set<String> visited) {
        if (SERIALIZABLE.equals(name)) {
            return true;
        }
        if (!visited.add(name)) {
            return false;
        }
        ClassInfo info = find(name);
        if (info == null) {
            return null;
        }
        boolean unknown = false;
        for (String type : info.getInterfaces()) {
            Boolean serializable = isSerializable(type, visited);
            if (Boolean.TRUE.equals(serializable)) {
                return true;
            }
            unknown |= serializable == null;
        }
        if (info.getSuperName() != null) {
            Boolean serializable = isSerializable(info.getSuperName(), visited);
            if (Boolean.TRUE.equals(serializable)) {
                return true;
            }
            unknown |= serializable == null;
        }
        return unknown ? null : false;
    }

}
