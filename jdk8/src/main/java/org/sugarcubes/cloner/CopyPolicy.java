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

import java.util.List;

/**
 * Copy policy interface.
 *
 * @author Maxim Butov
 */
@FunctionalInterface
public interface CopyPolicy<I> {

    /**
     * Returns action to apply to an input, which may be type, field or something else.
     * Must return non-null value.
     *
     * @param input input object
     * @return copy action
     */
    CopyAction getAction(I input);

    /**
     * Returns default copy policy.
     *
     * @param <I> input object type
     * @return default copy policy
     */
    static <I> CopyPolicy<I> defaultPolicy() {
        return input -> CopyAction.DEFAULT;
    }

    /**
     * Returns compound or single policy depending on list size.
     * Compound copy policy searches for the first non-default action in the list of policies.
     *
     * @param <I> input type
     * @param policies list of policies
     * @return compound or single policy
     */
    static <I> CopyPolicy<I> compound(List<CopyPolicy<I>> policies) {
        switch (policies.size()) {
            case 0:
                return defaultPolicy();
            case 1:
                return policies.iterator().next();
            default:
                return input -> policies.stream()
                    .map(p -> p.getAction(input))
                    .filter(action -> action != CopyAction.DEFAULT)
                    .findFirst()
                    .orElse(CopyAction.DEFAULT);
        }
    }

}
