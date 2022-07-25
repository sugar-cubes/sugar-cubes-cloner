package org.sugarcubes.cloner;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.ByteStreams;

/**
 * @author Maxim Butov
 */
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

            byte[] bytes;

            try (InputStream is = getResourceAsStream(resourceName)) {
                bytes = ByteStreams.toByteArray(is);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

            return defineClass(name, bytes, 0, bytes.length);
        }

        return super.loadClass(name, resolve);
    }

}
