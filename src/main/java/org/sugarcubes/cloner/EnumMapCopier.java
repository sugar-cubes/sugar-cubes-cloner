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

import java.util.EnumMap;
import java.util.Map;

/**
 * Copier for {@link EnumMap}.
 *
 * @author Maxim Butov
 */
public class EnumMapCopier<K extends Enum<K>, V> extends TwoPhaseObjectCopier<EnumMap<K, V>> {

    @Override
    public EnumMap<K, V> allocate(EnumMap<K, V> original) throws Exception {
        return original.clone();
    }

    @Override
    public void deepCopy(EnumMap<K, V> original, EnumMap<K, V> clone, CopyContext context) throws Exception {
        for (Map.Entry<K, V> entry : clone.entrySet()) {
            entry.setValue(context.copy(entry.getValue()));
        }
    }

}
