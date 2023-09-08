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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copy policy interface.
 *
 * @author Maxim Butov
 */
public interface CopyPolicy<I> {

    /**
     * Default copy policy.
     */
    CopyPolicy<?> DEFAULT = input -> CopyAction.DEFAULT;

    /**
     * Copy policy which always uses originals.
     */
    CopyPolicy<?> ORIGINAL = input -> CopyAction.ORIGINAL;

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
        return (CopyPolicy<I>) DEFAULT;
    }

    /**
     * Returns copy policy which always uses originals.
     *
     * @param <I> input object type
     * @return copy policy which always uses originals
     */
    static <I> CopyPolicy<I> original() {
        return (CopyPolicy<I>) ORIGINAL;
    }

    /**
     * Returns compound or single policy depending on list size.
     * Compound copy policy searches for the first non-default action in the list of policies.
     *
     * @param <I> input type
     * @param policies list of policies
     * @return compound or single policy
     */
    @SafeVarargs
    static <I> CopyPolicy<I> compound(CopyPolicy<I>... policies) {
        return compound(Arrays.asList(policies));
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
        List<CopyPolicy<I>> nonDefaultPolicies = policies.stream().filter(p -> p != DEFAULT).collect(Collectors.toList());
        switch (nonDefaultPolicies.size()) {
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
