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

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Copy context with recursive invocations.
 *
 * @author Maxim Butov
 */
public class RecursiveCopyContext extends AbstractCopyContext {

    /**
     * Creates an instance.
     *
     * @param copierProvider copier provider
     * @param clones predefined cloned objects
     */
    public RecursiveCopyContext(CopierProvider copierProvider, Map<Object, Object> clones) {
        super(copierProvider, clones);
    }

    @Override
    public void thenInvoke(Callable<?> task) throws Exception {
        task.call();
    }

    @Override
    public void complete() throws Exception {
    }

}
