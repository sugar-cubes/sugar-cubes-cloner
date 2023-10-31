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

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import io.github.sugarcubes.cloner.ClassUtils;

/**
 * Instance allocator factory which uses Objenesis library to create objects.
 *
 * @author Maxim Butov
 */
public class ObjenesisInstanceAllocatorFactory implements InstanceAllocatorFactory {

    /**
     * Availability of Objenesis.
     *
     * @return true, if Objenesis library is present in classpath
     */
    public static boolean isAvailable() {
        return ClassUtils.isClassAvailable("org.objenesis.ObjenesisStd");
    }

    /**
     * Objenesis instance.
     */
    private final Objenesis objenesis;

    /**
     * Default constructor.
     */
    public ObjenesisInstanceAllocatorFactory() {
        this(new ObjenesisStd());
    }

    /**
     * Constructor with an {@link Objenesis} instance.
     *
     * @param objenesis {@link Objenesis} instance
     */
    public ObjenesisInstanceAllocatorFactory(Objenesis objenesis) {
        this.objenesis = objenesis;
    }

    @Override
    public <T> InstanceAllocator<T> newAllocator(Class<T> type) {
        return objenesis.getInstantiatorOf(type)::newInstance;
    }

}
