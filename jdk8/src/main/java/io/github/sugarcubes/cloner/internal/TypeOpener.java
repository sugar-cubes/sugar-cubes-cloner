package io.github.sugarcubes.cloner.internal;

import io.github.sugarcubes.cloner.ClassUtils;

public interface TypeOpener {

    TypeOpener NOOP = new TypeOpener() {
        @Override
        public <T> Class<T> open(Class<T> type) {
            return type;
        }
    };

    <T> Class<T> open(Class<T> type);

    default <T> Class<T> open(String type) {
        return open(ClassUtils.classForName(type));
    }

}
