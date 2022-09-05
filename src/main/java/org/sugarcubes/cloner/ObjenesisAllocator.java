/**
 * Copyright 2017-2022 the original author or authors.
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

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

/**
 * Allocator which uses Objenesis library to create objects.
 *
 * @author Maxim Butov
 */
public class ObjenesisAllocator implements ObjectAllocator {

    /**
     * Objenesis instance.
     */
    private final Objenesis objenesis;

    /**
     * Default constructor.
     */
    public ObjenesisAllocator() {
        this(new ObjenesisStd());
    }

    /**
     * Constructor with an {@link Objenesis} instance.
     *
     * @param objenesis {@link Objenesis} instance
     */
    public ObjenesisAllocator(Objenesis objenesis) {
        this.objenesis = objenesis;
    }

    @Override
    public <T> T newInstance(Class<T> type) {
        return objenesis.newInstance(type);
    }

    @Override
    public <T> ObjectFactory<T> getFactory(Class<T> type) {
        return objenesis.getInstantiatorOf(type)::newInstance;
    }

}
