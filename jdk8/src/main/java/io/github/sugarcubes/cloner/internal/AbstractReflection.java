/*
 * Copyright 2017-2023 the original author or authors.
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
package io.github.sugarcubes.cloner.internal;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import static io.github.sugarcubes.cloner.ClonerExceptionUtils.replaceException;

public abstract class AbstractReflection implements Reflection {

    @Override
    public final Field[] getDeclaredFields(Class<?> type) {
        return replaceException(() -> getDeclaredFieldsImpl(type));
    }

    @Override
    public final Field getDeclaredField(Class<?> type, String name) {
        return replaceException(() -> getDeclaredFieldImpl(type, name));
    }

    @Override
    public final Method getDeclaredMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        return replaceException(() -> getDeclaredMethodImpl(type, name, parameterTypes));
    }

    @Override
    public final <T> Constructor<T> getDeclaredConstructor(Class<T> type, Class<?>... parameterTypes) {
        return replaceException(() -> getDeclaredConstructorImpl(type, parameterTypes));
    }

    @Override
    public final <T extends AccessibleObject> T makeAccessible(T member) {
        return replaceException(() -> makeAccessibleImpl(member));
    }

    protected Field getDeclaredFieldImpl(Class<?> type, String name) throws Exception {
        return Arrays.stream(getDeclaredFieldsImpl(type))
            .filter(field -> field.getName().equals(name))
            .findAny()
            .orElseThrow(() -> new NoSuchFieldException(String.format("No field '%s' in %s.", name, type)));
    }

    protected abstract Field[] getDeclaredFieldsImpl(Class<?> type) throws Exception;

    protected abstract Method getDeclaredMethodImpl(Class<?> type, String name, Class<?>... parameterTypes) throws Exception;

    protected abstract <T> Constructor<T> getDeclaredConstructorImpl(Class<T> type, Class<?>... parameterTypes) throws Exception;

    protected abstract <T extends AccessibleObject> T makeAccessibleImpl(T member);

}
