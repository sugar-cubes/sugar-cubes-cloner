/*
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

import java.lang.reflect.Field;

/**
 * Set of rules for objects cloning. These rules can be applied to types and fields. For more flexible
 * logic use {@link ObjectPolicy}.
 *
 * @see ObjectPolicy
 *
 * @author Maxim Butov
 */
public interface CopyPolicy {

    /**
     * Returns action to apply to an instance of the type. Must return non-null value.
     *
     * @param type type
     * @return action
     */
    default CopyAction getTypeAction(Class<?> type) {
        return CopyAction.DEFAULT;
    }

    /**
     * Returns action to apply to a field value. Must return non-null value.
     *
     * @param field field
     * @return action
     */
    default FieldCopyAction getFieldAction(Field field) {
        return FieldCopyAction.DEFAULT;
    }

}
