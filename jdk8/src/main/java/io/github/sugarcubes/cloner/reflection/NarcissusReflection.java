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

import io.github.sugarcubes.cloner.util.ClonerExceptionUtils;
import io.github.sugarcubes.cloner.util.Lazy;
import io.github.toolfactory.narcissus.Narcissus;

/**
 * Reflection on the Narcissus library (JNI): members are found without the filters of the JDK reflection and made
 * accessible by setting their accessible flag directly, so no package is opened and modules are not changed; only the
 * members the cloner uses become accessible. Loading this class needs Narcissus.
 *
 * @author Maxim Butov
 */
public class NarcissusReflection extends AbstractReflection {

    /**
     * {@code AccessibleObject.override}, the flag {@link AccessibleObject#setAccessible(boolean)} sets; hidden from
     * the JDK reflection since JDK 12.
     */
    private static final Lazy<Field> OVERRIDE = new Lazy<>(
        () -> ClonerExceptionUtils.replaceException(() -> Narcissus.findField(AccessibleObject.class, "override")));

    @Override
    protected Field[] getDeclaredFieldsImpl(Class<?> type) {
        return Narcissus.getDeclaredFields(type);
    }

    @Override
    protected Method getDeclaredMethodImpl(Class<?> type, String name, Class<?>... parameterTypes)
        throws NoSuchMethodException {
        return Arrays.stream(Narcissus.getDeclaredMethods(type))
            .filter(method -> method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parameterTypes))
            .findFirst()
            .orElseThrow(() -> new NoSuchMethodException(String.format("No method %s%s in %s.", name,
                Arrays.toString(parameterTypes), type.getName())));
    }

    @Override
    protected <T> Constructor<T> getDeclaredConstructorImpl(Class<T> type, Class<?>... parameterTypes)
        throws NoSuchMethodException {
        return Arrays.stream(Narcissus.getDeclaredConstructors(type))
            .filter(constructor -> Arrays.equals(constructor.getParameterTypes(), parameterTypes))
            .findFirst()
            .orElseThrow(() -> new NoSuchMethodException(String.format("No constructor %s in %s.",
                Arrays.toString(parameterTypes), type.getName())));
    }

    @Override
    protected <T extends AccessibleObject & Member> T makeAccessibleImpl(T member) {
        Narcissus.setBooleanField(member, OVERRIDE.get(), true);
        return member;
    }

    @Override
    protected <T extends AccessibleObject & Member> boolean tryMakeAccessibleImpl(T member) {
        makeAccessibleImpl(member);
        return true;
    }

}
