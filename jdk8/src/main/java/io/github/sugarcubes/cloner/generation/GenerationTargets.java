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
package io.github.sugarcubes.cloner.generation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import io.github.sugarcubes.cloner.ClonerException;

/**
 * Classes whose copiers are generated, listed by masks (see {@link ClassNameFilter}) in the resources
 * {@value #RESOURCE} of all jars visible to the class loader of a class: a project lists its own classes and classes
 * of libraries, a library may list its own classes.
 *
 * <p>The resource is a text file in UTF-8, one mask per line, {@code #} starts a comment:</p>
 * <pre>
 * com.acme.model.**
 * org.lib.money.Amount
 * !com.acme.model.cache.**
 * </pre>
 *
 * @author Maxim Butov
 */
public final class GenerationTargets {

    /**
     * Name of the resources with the masks.
     */
    public static final String RESOURCE = "META-INF/sugar-cubes-cloner/generated-copiers";

    /**
     * Filters of the class loaders, read once.
     */
    private static final Map<ClassLoader, ClassNameFilter> FILTERS = Collections.synchronizedMap(new WeakHashMap<>());

    private GenerationTargets() {
    }

    /**
     * Tells whether the copier of the class is to be generated. Classes of the bootstrap class loader (JDK) never are.
     *
     * @param type class
     * @return {@code true} if the class matches the masks of its class loader
     */
    public static boolean isTarget(Class<?> type) {
        ClassLoader loader = type.getClassLoader();
        return loader != null && FILTERS.computeIfAbsent(loader, GenerationTargets::read).matches(type.getName());
    }

    /**
     * Reads the masks of all resources visible to the class loader.
     *
     * @param loader class loader
     * @return filter
     */
    static ClassNameFilter read(ClassLoader loader) {
        List<String> masks = new ArrayList<>();
        try {
            Enumeration<URL> resources = loader.getResources(RESOURCE);
            while (resources.hasMoreElements()) {
                masks.addAll(parse(resources.nextElement()));
            }
        }
        catch (IOException e) {
            throw new ClonerException(String.format("Cannot read %s.", RESOURCE), e);
        }
        return masks.isEmpty() ? ClassNameFilter.NONE : ClassNameFilter.of(masks);
    }

    private static List<String> parse(URL resource) throws IOException {
        List<String> masks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream(),
            StandardCharsets.UTF_8))) {
            for (String line; (line = reader.readLine()) != null; ) {
                int comment = line.indexOf('#');
                String mask = (comment >= 0 ? line.substring(0, comment) : line).trim();
                if (!mask.isEmpty()) {
                    masks.add(mask);
                }
            }
        }
        return masks;
    }

}
