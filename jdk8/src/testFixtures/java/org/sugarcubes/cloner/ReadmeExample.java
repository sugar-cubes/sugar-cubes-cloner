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

import java.lang.reflect.Modifier;

class ReadmeExample {

    private final SomeObject original = new SomeObject();

    private void simple() {
        SomeObject clone = Cloners.reflection().clone(original);
    }

    private void doNotCloneType() {
        Cloner cloner = Cloners.builder()
            .typeAction(NonCloneableType.class, CopyAction.NULL)
            .build();
        SomeObject clone = cloner.clone(original);
    }

    private void skipTransientFields () {
        Cloner cloner = Cloners.builder()
            .fieldAction(field -> Modifier.isTransient(field.getModifiers()), CopyAction.SKIP)
            .build();
        SomeObject clone = cloner.clone(original);
    }

    private void skipTransientHibernateFields () {
        Cloner cloner = Cloners.builder()
            .fieldAction(Predicates.annotatedWith(Transient.class), CopyAction.SKIP)
            .build();
        SomeObject clone = cloner.clone(original);
    }

    private void example() {

        Cloner cloner =
            // new builder instance
            Cloners.builder()
                // custom allocator
                .objectFactoryProvider(new ObjenesisObjectFactoryProvider())
                // copy thread locals by reference
                .typeAction(Predicates.subclass(ThreadLocal.class), CopyAction.ORIGINAL)
                // do not clone closeables
                .typeAction(Predicates.subclass(AutoCloseable.class), CopyAction.ORIGINAL)
                // skip SomeObject.cachedValue field when cloning
                .fieldAction(SomeObject.class, "cachedValue", CopyAction.SKIP)
                // set fields with @Transient annotation to null
                .fieldAction(Predicates.annotatedWith(Transient.class), CopyAction.NULL)
                // custom copier for SomeOtherObject type
                .copier(CustomObject.class, new CustomObjectCopier())
                // parallel mode
                .mode(CloningMode.PARALLEL)
                // create cloner
                .build();

        // perform cloning
        SomeObject clone = cloner.clone(original);

    }

    private static class SomeObject {

    }

    private static class CustomObject {

    }

    @TypeCopier(NoopCopier.class)
    private static class NonCloneableType {

    }

    private static class CustomObjectCopier implements ObjectCopier<CustomObject> {

        @Override
        public CustomObject copy(CustomObject original, CopyContext context) throws Exception {
            throw new UnsupportedOperationException();
        }
    }

    private @interface Transient {

    }

}
