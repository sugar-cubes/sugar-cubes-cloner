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

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Implementation of {@link ObjectPolicy}.
 *
 * @author Maxim Butov
 */
public class CustomObjectPolicy implements ObjectPolicy {

    /**
     * Map (object, action).
     */
    private final Map<Object, CopyAction> objectActions;

    /**
     * Creates an instance.
     *
     * @param objectActions map (object, action)
     */
    public CustomObjectPolicy(Map<Object, CopyAction> objectActions) {
        this.objectActions = new IdentityHashMap<>(objectActions);
        this.objectActions.entrySet().removeIf(entry -> entry.getValue() == CopyAction.DEFAULT);
        Check.illegalArg(this.objectActions.isEmpty(), "Action map must contain at least one non-default action.");
    }

    @Override
    public CopyAction getObjectAction(Object original) {
        return objectActions.getOrDefault(original, CopyAction.DEFAULT);
    }

}
