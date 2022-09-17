/*
 * Copyright 2017-2022 the original author or authors.
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

class ReadmeExample {

    private void example() {

        SomeObject myObject = new SomeObject();

        Cloner cloner =
            // new builder instance
            Cloners.builder()
                // custom allocator
                .setAllocator(new ObjenesisAllocator())
                // copy thread locals by reference
                .setTypeAction(ThreadLocal.class, CopyAction.ORIGINAL)
                // skip SomeObject.cachedValue field when cloning
                .setFieldAction(SomeObject.class, "cachedValue", CopyAction.SKIP)
                // custom copier for SomeOtherObject type
                .setCopier(SomeOtherObject.class, new SomeOtherObjectCopier())
                // parallel mode
                .setMode(CloningMode.PARALLEL)
                // create cloner
                .build();

        // perform cloning
        SomeObject myObjectClone = cloner.clone(myObject);

    }

    private static class SomeObject {

    }

    private static class SomeOtherObject {

    }

    private static class SomeOtherObjectCopier implements ObjectCopier<SomeOtherObject> {

        @Override
        public SomeOtherObject copy(SomeOtherObject original, CopyContext context) throws Exception {
            throw new UnsupportedOperationException();
        }
    }
}
