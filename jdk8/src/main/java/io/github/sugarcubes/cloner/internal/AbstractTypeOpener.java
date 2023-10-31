package io.github.sugarcubes.cloner.internal;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import io.github.sugarcubes.cloner.ClonerExceptionUtils;

public abstract class AbstractTypeOpener implements TypeOpener {

    private final Set<Class<?>> opened = Collections.newSetFromMap(new WeakHashMap<>());

    @Override
    public <T> Class<T> open(Class<T> type) {
        if (opened.add(type)) {
            ClonerExceptionUtils.replaceException(() -> {
                openImpl(type);
                return null;
            });
        }
        return type;
    }

    protected abstract void openImpl(Class<?> type) throws Exception;

}
