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
package io.github.sugarcubes.cloner.generation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import io.github.sugarcubes.cloner.ClonerException;

/**
 * Filter of class names by masks in the syntax of ProGuard/R8 class filters.
 *
 * <ul>
 *     <li>{@code ?} - any character but a dot;</li>
 *     <li>{@code *} - any characters but dots, i.e. within a package;</li>
 *     <li>{@code **} - any characters, including dots, i.e. subpackages too;</li>
 *     <li>{@code !} before a mask - exclusion.</li>
 * </ul>
 *
 * <p>Names are binary names: {@code com.acme.Outer$Inner}. Unlike ProGuard the order of masks does not matter, masks
 * come from several jars in no particular order: a name matches if it matches some mask and no exclusion.</p>
 *
 * @author Maxim Butov
 */
public final class ClassNameFilter {

    /**
     * Filter which matches nothing.
     */
    public static final ClassNameFilter NONE = new ClassNameFilter(new ArrayList<>(), new ArrayList<>());

    private final List<Pattern> includes;

    private final List<Pattern> excludes;

    private ClassNameFilter(List<Pattern> includes, List<Pattern> excludes) {
        this.includes = includes;
        this.excludes = excludes;
    }

    /**
     * Creates the filter from masks.
     *
     * @param masks masks, exclusions start with {@code !}
     * @return filter
     */
    public static ClassNameFilter of(Collection<String> masks) {
        List<Pattern> includes = new ArrayList<>();
        List<Pattern> excludes = new ArrayList<>();
        for (String mask : masks) {
            if (mask.startsWith("!")) {
                excludes.add(compile(mask.substring(1)));
            }
            else {
                includes.add(compile(mask));
            }
        }
        return new ClassNameFilter(includes, excludes);
    }

    private static Pattern compile(String mask) {
        if (mask.isEmpty() || !mask.matches("[\\w.$?*]+")) {
            throw new ClonerException(String.format("Invalid class name mask '%s'.", mask));
        }
        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < mask.length(); i++) {
            char c = mask.charAt(i);
            if (c == '*') {
                boolean any = i + 1 < mask.length() && mask.charAt(i + 1) == '*';
                regex.append(any ? ".*" : "[^.]*");
                i += any ? 1 : 0;
            }
            else if (c == '?') {
                regex.append("[^.]");
            }
            else {
                regex.append(Pattern.quote(String.valueOf(c)));
            }
        }
        return Pattern.compile(regex.toString());
    }

    /**
     * Tells whether the name matches some mask and no exclusion.
     *
     * @param className binary class name
     * @return {@code true} if the name matches
     */
    public boolean matches(String className) {
        return includes.stream().anyMatch(pattern -> pattern.matcher(className).matches())
            && excludes.stream().noneMatch(pattern -> pattern.matcher(className).matches());
    }

}
