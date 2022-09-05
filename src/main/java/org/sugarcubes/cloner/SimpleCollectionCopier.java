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

import java.util.Arrays;
import java.util.Collection;
import java.util.function.IntFunction;

/**
 * Copier for simple collections (that do not call items {@link Object#hashCode()} method), because item hash code can differ in
 * the beginning and in the end of cloning.
 *
 * @author Maxim Butov
 */
public class SimpleCollectionCopier<T extends Collection<Object>> extends TwoPhaseObjectCopier<T> {

    /**
     * Collection constructor with size argument.
     */
    private final IntFunction<T> constructor;

    /**
     * Creates copier.
     *
     * @param constructor collection constructor
     */
    public SimpleCollectionCopier(IntFunction<T> constructor) {
        this.constructor = constructor;
    }

    @Override
    public T allocate(T original) throws Exception {
        return constructor.apply(original.size());
    }

    @Override
    public void deepCopy(T original, T clone, CopyContext context) throws Exception {
        Object[] array = original.toArray();
        for (int k = 0, length = array.length; k < length; k++) {
            array[k] = context.copy(array[k]);
        }
        clone.addAll(Arrays.asList(array));
    }

}
