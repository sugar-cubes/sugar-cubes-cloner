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

import java.util.HashMap;
import java.util.Map;

/**
 * Context which is used by single thread.
 *
 * @author Maxim Butov
 */
public abstract class AbstractSingleThreadCopyContext extends AbstractCopyContext {

    /**
     * Counter of objects to be cloned. Increased on the start of cloning, decreased on registration.
     */
    private final int[] counter = new int[1];

    /**
     * Creates context with specified copier provider and predefined cloned objects.
     *
     * @param copierProvider copier provider
     * @param clones predefined cloned objects
     */
    public AbstractSingleThreadCopyContext(CopierProvider copierProvider, Map<Object, Object> clones) {
        super(copierProvider, HashMap::new, clones);
    }

    @Override
    protected int[] counter() {
        return counter;
    }

}
