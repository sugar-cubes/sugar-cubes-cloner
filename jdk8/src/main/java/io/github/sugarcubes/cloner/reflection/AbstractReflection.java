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
package io.github.sugarcubes.cloner.reflection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import io.github.sugarcubes.cloner.spi.Reflection;
import static io.github.sugarcubes.cloner.util.ClonerExceptionUtils.replaceException;

/**
 * Base reflection: wraps exceptions of implementation methods into {@link io.github.sugarcubes.cloner.ClonerException}.
 *
 * @author Maxim Butov
 */
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
    public final <T extends AccessibleObject & Member> T makeAccessible(T member) {
        return replaceException(() -> makeAccessibleImpl(member));
    }

    /**
     * Finds the declared field among {@link #getDeclaredFieldsImpl(Class) declared fields}.
     *
     * @param type class
     * @param name field name
     * @return field
     * @throws Exception if there is no such field
     */
    protected Field getDeclaredFieldImpl(Class<?> type, String name) throws Exception {
        return Arrays.stream(getDeclaredFieldsImpl(type))
            .filter(field -> field.getName().equals(name))
            .findAny()
            .orElseThrow(() -> new NoSuchFieldException(String.format("No field '%s' in %s.", name, type)));
    }

    /**
     * Returns the declared fields of the class.
     *
     * @param type class
     * @return declared fields
     * @throws Exception if fields cannot be obtained
     */
    protected abstract Field[] getDeclaredFieldsImpl(Class<?> type) throws Exception;

    /**
     * Returns the declared method of the class.
     *
     * @param type class
     * @param name method name
     * @param parameterTypes parameter types
     * @return method
     * @throws Exception if there is no such method
     */
    protected abstract Method getDeclaredMethodImpl(Class<?> type, String name, Class<?>... parameterTypes) throws Exception;

    /**
     * Returns the declared constructor of the class.
     *
     * @param <T> object type
     * @param type class
     * @param parameterTypes parameter types
     * @return constructor
     * @throws Exception if there is no such constructor
     */
    protected abstract <T> Constructor<T> getDeclaredConstructorImpl(Class<T> type, Class<?>... parameterTypes) throws Exception;

    /**
     * Makes the member accessible.
     *
     * @param <T> member type
     * @param member member
     * @return same member
     */
    protected abstract <T extends AccessibleObject & Member> T makeAccessibleImpl(T member);

    @Override
    public final <T extends AccessibleObject & Member> Optional<T> tryMakeAccessible(T member) {
        return tryMakeAccessibleImpl(member) ? Optional.of(member) : Optional.empty();
    }

    /**
     * Tries to make the member accessible.
     *
     * @param <T> member type
     * @param member member
     * @return {@code true} if the member is accessible now
     */
    protected abstract <T extends AccessibleObject & Member> boolean tryMakeAccessibleImpl(T member);

}
