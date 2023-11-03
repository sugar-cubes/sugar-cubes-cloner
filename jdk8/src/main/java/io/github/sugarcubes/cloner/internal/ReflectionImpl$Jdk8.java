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

public class ReflectionImpl$Jdk8 extends AbstractReflection {

    @Override
    protected Field getDeclaredFieldImpl(Class<?> type, String name) throws Exception {
        return type.getDeclaredField(name);
    }

    @Override
    protected Field[] getDeclaredFieldsImpl(Class<?> type) throws Exception {
        return type.getDeclaredFields();
    }

    @Override
    protected Method getDeclaredMethodImpl(Class<?> type, String name, Class<?>... parameterTypes) throws Exception {
        return type.getDeclaredMethod(name, parameterTypes);
    }

    @Override
    protected <T> Constructor<T> getDeclaredConstructorImpl(Class<T> type, Class<?>... parameterTypes) throws Exception {
        return type.getDeclaredConstructor(parameterTypes);
    }

    @Override
    protected <T extends AccessibleObject> T makeAccessibleImpl(T member) {
        member.setAccessible(true);
        return member;
    }

}
