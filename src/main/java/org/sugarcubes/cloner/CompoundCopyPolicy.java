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
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Compound copy policy. Searches for the first non-default action in the list of policies.
 *
 * @author Maxim Butov
 */
public class CompoundCopyPolicy implements CopyPolicy {

    /**
     * List of policies.
     */
    private final CopyPolicy[] policies;

    /**
     * Creates copy policy.
     *
     * @param policies list of policies
     */
    public CompoundCopyPolicy(List<CopyPolicy> policies) {
        this.policies = policies.toArray(new CopyPolicy[0]);
    }

    /**
     * Finds the first non-default action in the list of policies.
     *
     * @param <A> action enum type
     * @param mapping method of policy returning action
     * @param defaultAction default action
     * @return first non-default action if found, or default if not
     */
    private <A extends Enum<A>> A findAction(Function<CopyPolicy, A> mapping, A defaultAction) {
        return Arrays.stream(policies)
            .map(mapping)
            .filter(action -> action != defaultAction)
            .findFirst()
            .orElse(defaultAction);
    }

    @Override
    public CopyAction getTypeAction(Class<?> type) {
        return findAction(policy -> policy.getTypeAction(type), CopyAction.DEFAULT);
    }

    @Override
    public FieldCopyAction getFieldAction(Field field) {
        return findAction(policy -> policy.getFieldAction(field), FieldCopyAction.DEFAULT);
    }

}
