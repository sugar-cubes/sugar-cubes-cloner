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

import java.lang.reflect.Array;

/**
 * Two-phase copier of an array of objects.
 *
 * @author Maxim Butov
 */
public class ObjectArrayCopier extends TwoPhaseObjectCopier<Object[]> {

    @Override
    public Object[] allocate(Object[] original) {
        Class<?> componentType = original.getClass().getComponentType();
        return componentType == Object.class ? new Object[original.length] :
            (Object[]) Array.newInstance(componentType, original.length);
    }

    @Override
    public void deepCopy(Object[] original, Object[] clone, CopyContext context) throws Exception {
        for (int k = 0, length = original.length; k < length; k++) {
            clone[k] = context.copy(original[k]);
        }
    }

}
