/*
 * Copyright 2017-2023 the original author or authors.
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
package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import static org.sugarcubes.cloner.ClonerExceptionUtils.replaceException;

/**
 * Implementation of {@link JdkConfiguration} for JDK 9+.
 */
class Jdk9ConfigurationImpl extends Jdk8ConfigurationImpl {

    private static final List<Object> SYSTEM_WIDE_SINGLETONS = replaceException(() -> {
        Class<?> immutableCollectionsClass = ReflectionUtils.classForName("java.util.ImmutableCollections");
        String archivedObjectsFieldName = "archivedObjects";
        try {
            immutableCollectionsClass.getDeclaredField(archivedObjectsFieldName);
        }
        catch (NoSuchFieldException e) {
            return List.of();
        }
        Field archivedObjectsField = ReflectionUtils.getField(immutableCollectionsClass, archivedObjectsFieldName);
        Object[] archivedObjects = (Object[]) archivedObjectsField.get(null);
        return List.of(archivedObjects);
    });

    @Override
    public Collection<Object> getSystemWideSingletons() {
        return SYSTEM_WIDE_SINGLETONS;
    }

}
