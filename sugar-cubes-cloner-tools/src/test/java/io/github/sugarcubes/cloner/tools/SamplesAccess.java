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

import java.nio.file.Path;
import java.util.List;

/**
 * Access to {@link Samples} from other packages of the tests.
 */
public final class SamplesAccess {

    private SamplesAccess() {
    }

    public static Path sources() {
        return Samples.sources().resolve(Samples.PATH);
    }

    public static List<String> masks() {
        return Samples.MASKS;
    }

    public static void writeMasks(Path root) throws Exception {
        Samples.writeMasks(root);
    }

    public static void copyClasses(Path root) throws Exception {
        Samples.copyClasses(root);
    }

    public static byte[] classFile(Path root, String simpleName) throws Exception {
        return Samples.classFile(root, simpleName);
    }

    public static void assertEnhanced(Path root) throws Exception {
        Samples.assertEnhanced(root);
    }

    public static void assertClonedByCopyConstructors(Path root) throws Exception {
        Samples.assertClonedByCopyConstructors(root);
    }

}
