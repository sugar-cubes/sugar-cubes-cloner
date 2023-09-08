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
package org.sugarcubes.cloner;

import sun.misc.Unsafe;

/**
 * Object factory provider which uses {@link Unsafe} to create object instance.
 *
 * @author Maxim Butov
 */
public class UnsafeObjectFactoryProvider implements ObjectFactoryProvider {

    /**
     * {@link UnsafeBridge} instance.
     */
    private final UnsafeBridge unsafe = JdkConfigurationHolder.CONFIGURATION.getUnsafe();

    @Override
    public <T> ObjectFactory<T> getFactory(Class<T> type) {
        return () -> unsafe.allocateInstance(type);
    }

}
