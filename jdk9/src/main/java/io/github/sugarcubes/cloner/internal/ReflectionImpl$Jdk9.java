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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * todo
 */
public class ReflectionImpl$Jdk9 extends ReflectionImpl$Jdk8 {

    private final TypeOpener opener;

    public ReflectionImpl$Jdk9(TypeOpener opener) {
        this.opener = opener;
    }

    @Override
    protected Field getDeclaredFieldImpl(Class<?> type, String name) throws Exception {
        opener.open(type);
        return super.getDeclaredFieldImpl(type, name);
    }

    @Override
    protected Field[] getDeclaredFieldsImpl(Class<?> type) throws Exception {
        opener.open(type);
        return super.getDeclaredFieldsImpl(type);
    }

    @Override
    protected Method getDeclaredMethodImpl(Class<?> type, String name, Class<?>... parameterTypes) throws Exception {
        opener.open(type);
        return super.getDeclaredMethodImpl(type, name, parameterTypes);
    }

    @Override
    protected <T> Constructor<T> getDeclaredConstructorImpl(Class<T> type, Class<?>... parameterTypes) throws Exception {
        opener.open(type);
        return super.getDeclaredConstructorImpl(type, parameterTypes);
    }

}
