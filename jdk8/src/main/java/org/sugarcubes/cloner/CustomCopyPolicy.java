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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Copy policy implementation.
 *
 * @author Maxim Butov
 */
public class CustomCopyPolicy implements CopyPolicy {

    /**
     * Map (type, action).
     */
    private final Map<Class<?>, CopyAction> typeActions;

    /**
     * Map (field, action).
     */
    private final Map<Field, FieldCopyAction> fieldActions;

    /**
     * Creates copy policy.
     *
     * @param typeActions  type actions
     * @param fieldActions field actions
     */
    public CustomCopyPolicy(Map<Class<?>, CopyAction> typeActions, Map<Field, FieldCopyAction> fieldActions) {
        this.typeActions = copyOfMap(typeActions, CopyAction.DEFAULT);
        this.fieldActions = copyOfMap(fieldActions, FieldCopyAction.DEFAULT);
    }

    /**
     * Creates copy of map with action values, removes entries with default action as value.
     *
     * @param <K> key type
     * @param <A> action type
     * @param map source map
     * @param defaultAction default action
     * @return filtered copy of the source map
     */
    private static <K, A extends Enum<A>> Map<K, A> copyOfMap(Map<K, A> map, A defaultAction) {
        Map<K, A> copy = new LinkedHashMap<>(map);
        copy.entrySet().removeIf(entry -> entry.getValue() == defaultAction);
        return copy.isEmpty() ? Collections.emptyMap() : copy;
    }

    @Override
    public CopyAction getTypeAction(Class<?> type) {
        return typeActions.getOrDefault(type, CopyAction.DEFAULT);
    }

    @Override
    public FieldCopyAction getFieldAction(Field field) {
        return fieldActions.getOrDefault(field, FieldCopyAction.DEFAULT);
    }

}
