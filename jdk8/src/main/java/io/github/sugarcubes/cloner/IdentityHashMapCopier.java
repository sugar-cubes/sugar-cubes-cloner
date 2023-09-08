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

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Copier for {@link IdentityHashMap}. {@link ReflectionCopier} is not suitable for {@link IdentityHashMap} because the result's
 * {@link IdentityHashMap#table} has its entries on wrong places because clones have different identity hash codes than original
 * objects. Thus, the map has to be entirely rebuilt.
 *
 * @author Maxim Butov
 */
public class IdentityHashMapCopier extends TwoPhaseObjectCopier<IdentityHashMap<Object, Object>> {

    @Override
    public IdentityHashMap<Object, Object> allocate(IdentityHashMap<Object, Object> original) throws Exception {
        return new IdentityHashMap<>(original.size());
    }

    @Override
    public void deepCopy(IdentityHashMap<Object, Object> original, IdentityHashMap<Object, Object> clone, CopyContext context)
        throws Exception {
        for (Map.Entry<Object, Object> entry : original.entrySet()) {
            clone.put(context.copy(entry.getKey()), context.copy(entry.getValue()));
        }
    }

}
