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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

/**
 * Tests of the cloning of immutable collections created with {@link List#of(Object[])}, {@link Set#of(Object[])}
 * and {@link Map#of(Object, Object)}.
 *
 * @author Maxim Butov
 */
public class ImmutableCollectionsTest {

    <T> void testEmptyCollectionClone(T original) {
        T clone = Cloners.reflection().clone(original);
        assertThat(clone, sameInstance(original));
    }

    <T> void testImmutableCollectionClone(T original) {
        T clone = Cloners.reflection().clone(original);
        assertThat(clone, not(sameInstance(original)));
        assertThat(clone, equalTo(original));
    }

    @Test
    void testImmutableCollections() {
        testEmptyCollectionClone(List.of());
        testImmutableCollectionClone(List.of(1));
        testImmutableCollectionClone(List.of(1, 2));
        testEmptyCollectionClone(Set.of());
        testImmutableCollectionClone(Set.of(1));
        testImmutableCollectionClone(Set.of(1, 2));
        testEmptyCollectionClone(Map.of());
        testImmutableCollectionClone(Map.of(1, 2));
    }

}
