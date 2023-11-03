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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.github.sugarcubes.cloner.ClassUtils;
import io.github.sugarcubes.cloner.ClonerExceptionUtils;
import io.github.sugarcubes.cloner.ReflectionInstanceAllocatorFactory;
import sun.misc.Unsafe;

public class EnvironmentImpl$Jdk8 implements Environment {

    protected static final boolean OBJENESIS = ClassUtils.isClassAvailable("org.objenesis.ObjenesisStd");

    protected List<TypeOpener> openers = new ArrayList<>();
    protected List<Reflection> reflections = new ArrayList<>();
    protected sun.misc.Unsafe unsafe;
    protected List<InstanceAllocatorFactory> allocators = new ArrayList<>();

    public EnvironmentImpl$Jdk8() {
        TypeOpener opener = TypeOpener.NOOP;
        openers.add(opener);
        ReflectionImpl$Jdk8 reflection = new ReflectionImpl$Jdk8();
        reflections.add(reflection);
        unsafe = ClonerExceptionUtils.replaceException(() ->getUnsafeImpl(opener, reflection));
    }

    @Override
    public TypeOpener getOpener() {
        return TypeOpener.NOOP;
    }

    @Override
    public sun.misc.Unsafe getUnsafe() {
        return ClonerExceptionUtils.replaceException(() -> getUnsafeImpl(null, null));
    }

    protected <T> T getFieldValue(Class<?> type, String fieldName, Object target) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    protected sun.misc.Unsafe getUnsafeImpl(TypeOpener opener, Reflection reflection) throws Exception {
        opener.open("sun.misc.Unsafe");
        Field theUnsafe = reflection.getDeclaredField(Unsafe.class, "theUnsafe");
        reflection.makeAccessible(theUnsafe);
        return getFieldValue(sun.misc.Unsafe.class, "theUnsafe", null);
    }

    @Override
    public Reflection getReflection() {
        return new ReflectionImpl$Jdk8();
    }

    @Override
    public InstanceAllocatorFactory getAllocator() {
        return OBJENESIS ? new ObjenesisInstanceAllocatorFactory() : new ReflectionInstanceAllocatorFactory();
    }

}
