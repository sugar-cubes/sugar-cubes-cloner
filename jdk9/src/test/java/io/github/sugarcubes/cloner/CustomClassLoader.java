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
package io.github.sugarcubes.cloner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CustomClassLoader extends ClassLoader {

    private final List<String> disabledPrefixes = new ArrayList<>();
    private final List<String> reloadedPrefixes = new ArrayList<>();

    public CustomClassLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public CustomClassLoader(ClassLoader parent) {
        super(parent);
    }

    public CustomClassLoader disable(String prefix) {
        disabledPrefixes.add(prefix);
        return this;
    }

    public CustomClassLoader reload(String prefix) {
        reloadedPrefixes.add(prefix);
        return this;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

        if (disabledPrefixes.stream().anyMatch(name::startsWith)) {
            throw new ClassNotFoundException(name);
        }

        boolean reload = reloadedPrefixes.stream().anyMatch(name::startsWith);

        if (reload) {
            String resourceName = name.replace(".", "/") + ".class";
            byte[] bytes = getResourceAsBytes(resourceName);
            return defineClass(name, bytes, 0, bytes.length);
        }

        return super.loadClass(name, resolve);
    }

    private byte[] getResourceAsBytes(String name) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (InputStream input = getResourceAsStream(name)) {
            byte[] bytes = new byte[1024];
            for (int count; (count = input.read(bytes)) > 0; ) {
                buffer.write(bytes, 0, count);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return buffer.toByteArray();
    }

}
