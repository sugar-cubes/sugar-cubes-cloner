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
package io.github.sugarcubes.cloner;

import java.util.HashMap;
import java.util.Map;

/**
 * Context which is used by single thread.
 *
 * @author Maxim Butov
 */
public abstract class AbstractSingleThreadCopyContext extends AbstractCopyContext {

    /**
     * Creates context with specified copier provider and predefined cloned objects.
     *
     * @param copierProvider copier provider
     * @param clones predefined cloned objects
     */
    public AbstractSingleThreadCopyContext(CopierProvider copierProvider, Map<Object, Object> clones) {
        super(copierProvider, HashMap::new, clones);
    }

}
