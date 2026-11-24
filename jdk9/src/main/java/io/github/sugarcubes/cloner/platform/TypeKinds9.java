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

import java.util.List;

/**
 * {@link TypeKinds} of JDK 9+: modules, module layers and process handles are objects of the running JVM, cleaners are
 * uncopyable.
 *
 * @author Maxim Butov
 */
class TypeKinds9 extends TypeKinds8 {

    /**
     * Objects of the running JVM which appeared in JDK 9, with all their subtypes.
     */
    private static final List<Class<?>> RUNTIME_SUPER_TYPES = unmodifiableList(Module.class, ModuleLayer.class,
        ProcessHandle.class);

    /**
     * Uncopyable types which appeared in JDK 9, with all their subtypes.
     */
    private static final List<Class<?>> UNCOPYABLE_SUPER_TYPES = unmodifiableList(java.lang.ref.Cleaner.class);

    @Override
    Kind kindOf(Class<?> type) {
        if (isSubtype(type, RUNTIME_SUPER_TYPES)) {
            return Kind.RUNTIME;
        }
        return isSubtype(type, UNCOPYABLE_SUPER_TYPES) ? Kind.UNCOPYABLE : super.kindOf(type);
    }

}
