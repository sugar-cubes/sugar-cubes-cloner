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
package io.github.sugarcubes.cloner;

import java.lang.reflect.Constructor;

import io.github.sugarcubes.cloner.internal.InstanceAllocator;
import io.github.sugarcubes.cloner.internal.InstanceAllocatorFactory;
import io.github.sugarcubes.cloner.internal.ObjenesisInstanceAllocatorFactory;

/**
 * Instance allocator factory which uses no-arg constructor to create object.
 *
 * @see ObjenesisInstanceAllocatorFactory
 *
 * @author Maxim Butov
 */
public class ReflectionInstanceAllocatorFactory implements InstanceAllocatorFactory {

    @Override
    public <T> InstanceAllocator<T> newAllocator(Class<T> type) {
        Constructor<T> constructor;
        try {
            constructor = ReflectionUtils.getConstructor(type);
        }
        catch (ClonerException e) {
            throw e.replaceIf(NoSuchMethodException.class,
                () -> String.format("No-arg constructor for %1$s does not exist. " +
                    "You may add Objenesis library into your classpath or use custom copier for %1$s.", type)
            );
        }
        return constructor::newInstance;
    }

}
