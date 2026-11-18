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
package io.github.sugarcubes.cloner.engine;

import io.github.sugarcubes.cloner.CopyContext;
import io.github.sugarcubes.cloner.ObjectCopier;
import io.github.sugarcubes.cloner.util.Checks;

/**
 * Copier which chooses the actual copier for every object, e.g. a single-phase or a two-phase one. The copier provider
 * returns the chosen copier, so this one never copies itself.
 *
 * @param <T> object type
 * @author Maxim Butov
 */
public abstract class ChoosingCopier<T> implements ObjectCopier<T> {

    /**
     * Chooses the copier for the object.
     *
     * @param original original object
     * @return copier of the object
     */
    public abstract ObjectCopier<T> choose(T original);

    @Override
    public T copy(T original, CopyContext context) throws Exception {
        throw Checks.mustNotHappen();
    }

}
