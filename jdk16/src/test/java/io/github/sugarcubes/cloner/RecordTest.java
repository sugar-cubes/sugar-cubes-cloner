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

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

/**
 * Tests of the cloning of records.
 *
 * @author Maxim Butov
 */
public class RecordTest {

    public record Cat(String name, int numberOfLives, String color) {

        public Cat {
        }

    }

    @Test
    void testRecords() {
        Cat cat1 = new Cat("Kesha", 7, "Gray");
        Cat cat2 = Cloners.reflection().clone(cat1);
        assertThat(cat2, not(sameInstance(cat1)));
        assertThat(cat2, equalTo(cat1));
    }

}
