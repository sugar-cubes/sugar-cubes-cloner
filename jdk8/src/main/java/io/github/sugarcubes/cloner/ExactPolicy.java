/*
 * Copyright 2017-2024 the original author or authors.
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

import java.util.Map;
import java.util.function.Function;

/**
 * Copy policy based on exact values.
 *
 * @see PredicatePolicy
 *
 * @author Maxim Butov
 */
public class ExactPolicy<I> extends AbstractMappedPolicy<I, I> {

    /**
     * Creates policy.
     *
     * @param map map (input, action)
     */
    public ExactPolicy(Map<I, CopyAction> map) {
        super(map);
    }

    /**
     * Creates policy.
     *
     * @param map map (input, action)
     * @param mapCopyConstructor copy constructor of map as method reference
     */
    public ExactPolicy(Map<I, CopyAction> map, Function<Map<I, CopyAction>, Map<I, CopyAction>> mapCopyConstructor) {
        super(map, mapCopyConstructor);
    }

    @Override
    public CopyAction getAction(I input) {
        return get(input);
    }

}
