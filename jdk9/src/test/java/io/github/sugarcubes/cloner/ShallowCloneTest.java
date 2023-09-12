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

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

/**
 * Tests shallow mode.
 *
 * @author Maxim Butov
 */
public class ShallowCloneTest {

    static class Shallow {
        Object inner = new Object();
    }

    static class Outer {
        Shallow shallow = new Shallow();
    }

    @Test
    void testShallow() {
        Outer original = new Outer();
        Outer clone = Cloners.builder().shallow(Shallow.class).build().clone(original);
        assertThat(clone, not(sameInstance(original)));
        assertThat(clone.shallow, not(sameInstance(original.shallow)));
        assertThat(clone.shallow.inner, sameInstance(original.shallow.inner));
    }

}
