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
package io.github.sugarcubes.cloner.tools.maven;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.sugarcubes.cloner.tools.ClassFiles;
import io.github.sugarcubes.cloner.tools.SamplesAccess;

class EnhanceMojoTest {

    @Test
    void enhancesClassesDirectory(@TempDir Path classes) throws Exception {
        SamplesAccess.copyClasses(classes);
        SamplesAccess.writeMasks(classes);

        mojo(classes, false).execute();

        SamplesAccess.assertEnhanced(classes);
        SamplesAccess.assertClonedByCopyConstructors(classes);
    }

    @Test
    void skip(@TempDir Path classes) throws Exception {
        SamplesAccess.copyClasses(classes);
        SamplesAccess.writeMasks(classes);

        mojo(classes, true).execute();

        Assertions.assertFalse(new String(SamplesAccess.classFile(classes, "Order"), StandardCharsets.ISO_8859_1)
            .contains("CopyConstructors"));
    }

    @Test
    void descriptorDescribesGoals() throws IOException {
        String descriptor;
        try (InputStream in = getClass().getResourceAsStream("/META-INF/maven/plugin.xml")) {
            descriptor = new String(ClassFiles.readAll(in), StandardCharsets.UTF_8);
        }

        Assertions.assertTrue(descriptor.contains("<goal>enhance</goal>"));
        Assertions.assertTrue(descriptor.contains("<goal>enhance-test</goal>"));
        Assertions.assertTrue(descriptor.contains("<implementation>" + EnhanceMojo.class.getName()));
        Assertions.assertFalse(descriptor.contains("@version@"));
    }

    private static EnhanceMojo mojo(Path classes, boolean skip) throws ReflectiveOperationException {
        EnhanceMojo mojo = new EnhanceMojo();
        set(mojo, "classesDirectory", classes.toFile());
        set(mojo, "classpathElements", Collections.singletonList(classes.toString()));
        set(mojo, "skip", skip);
        return mojo;
    }

    private static void set(EnhanceMojo mojo, String name, Object value) throws ReflectiveOperationException {
        // as Maven injects the parameters
        Field field = EnhanceMojo.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(mojo, value);
    }

}
