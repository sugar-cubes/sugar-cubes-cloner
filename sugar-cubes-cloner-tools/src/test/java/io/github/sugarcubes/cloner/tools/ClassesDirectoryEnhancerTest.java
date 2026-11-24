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

import java.io.ObjectStreamClass;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.sugarcubes.cloner.tools.CopyConstructorEnhancer.Status;

class ClassesDirectoryEnhancerTest {

    @Test
    void enhancesListedClasses(@TempDir Path dir) throws Exception {
        Samples.copyClasses(dir);
        List<String> warnings = new ArrayList<>();

        ClassesDirectoryEnhancer.Report report = ClassesDirectoryEnhancer.enhance(dir, Collections.emptyList(),
            Samples.MASKS, warnings::add);

        Assertions.assertEquals(Samples.ENHANCED.size(), report.count(Status.ENHANCED), report.toString());
        Assertions.assertEquals(Samples.SKIPPED.size(), report.count(Status.SKIPPED), report.toString());
        Assertions.assertEquals(Samples.SKIPPED.size(), warnings.size());
        Assertions.assertTrue(warnings.stream().anyMatch(w -> w.contains("ExtendsArrayList")
            && w.contains("java.util.ArrayList")), warnings.toString());
        Samples.assertEnhanced(dir);
    }

    @Test
    void enhancingIsIdempotent(@TempDir Path dir) throws Exception {
        Samples.copyClasses(dir);
        ClassesDirectoryEnhancer.enhance(dir, Collections.emptyList(), Samples.MASKS, warning -> { });

        ClassesDirectoryEnhancer.Report report = ClassesDirectoryEnhancer.enhance(dir, Collections.emptyList(),
            Samples.MASKS, warning -> { });

        Assertions.assertEquals(0, report.count(Status.ENHANCED));
        Assertions.assertEquals(Samples.ENHANCED.size(), report.count(Status.ALREADY_ENHANCED));
    }

    @Test
    void enhancedClassesAreClonedByCopyConstructors(@TempDir Path dir) throws Exception {
        Samples.copyClasses(dir);
        ClassesDirectoryEnhancer.enhance(dir, Collections.emptyList(), Samples.MASKS, warning -> { });

        Samples.assertClonedByCopyConstructors(dir);
    }

    @Test
    void serialVersionIsKept(@TempDir Path dir) throws Exception {
        Samples.copyClasses(dir);
        ClassesDirectoryEnhancer.enhance(dir, Collections.emptyList(), Samples.MASKS, warning -> { });
        ClassLoader loader = Samples.loader(dir);

        for (String name : new String[] {"Line", "DiscountLine"}) {
            Class<?> original = Class.forName(Samples.PACKAGE + "." + name);
            Class<?> enhanced = loader.loadClass(Samples.PACKAGE + "." + name);

            Assertions.assertNotSame(original, enhanced);
            Assertions.assertEquals(ObjectStreamClass.lookup(original).getSerialVersionUID(),
                ObjectStreamClass.lookup(enhanced).getSerialVersionUID(), name);
        }
    }

    @Test
    void noMasksNoChanges(@TempDir Path dir) throws Exception {
        Samples.copyClasses(dir);

        ClassesDirectoryEnhancer.Report report = ClassesDirectoryEnhancer.enhance(dir, Collections.emptyList(),
            Collections.emptyList(), warning -> { });

        Assertions.assertEquals(0, report.count(Status.ENHANCED));
    }

}
