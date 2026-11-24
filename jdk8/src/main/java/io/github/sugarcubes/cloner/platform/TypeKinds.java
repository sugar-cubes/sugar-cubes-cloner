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

/**
 * Kinds of types which decide how instances are copied by default.
 *
 * @author Maxim Butov
 */
public interface TypeKinds {

    /**
     * Tells whether instances of the type are immutable, so they are shared: values, canonical instances, stateless
     * singletons.
     *
     * @param type type
     * @return {@code true} for immutable types
     */
    boolean isImmutable(Class<?> type);

    /**
     * Tells whether instances of the type are copied with their public {@code clone()} method.
     *
     * @param type type
     * @return {@code true} for cloneable types
     */
    boolean isCloneable(Class<?> type);

    /**
     * Tells whether instances of the type are objects of the running JVM whose identity matters (class loaders,
     * modules, reflection objects, keys of thread-local values), so they are shared.
     *
     * @param type type
     * @return {@code true} for objects of the running JVM
     */
    boolean isRuntime(Class<?> type);

    /**
     * Tells whether instances of the type are live objects of the JVM or the operating system (threads, references,
     * open files, sockets, processes, executors): a clone cannot get its own one, and sharing must be chosen
     * explicitly, so copying them fails by default.
     *
     * @param type type
     * @return {@code true} for uncopyable types
     */
    boolean isUncopyable(Class<?> type);

    /**
     * Tells whether the class is hidden (a hidden class on JDK 15+ or a lambda class): it cannot be found by name, its
     * instances cannot be allocated by name and their final fields are not writable.
     *
     * @param type type
     * @return {@code true} for hidden classes
     */
    boolean isHidden(Class<?> type);

}
