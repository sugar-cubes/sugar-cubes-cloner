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

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.ObjenesisObjectFactory;
import org.junit.jupiter.api.Test;

public class RandomObjectTest {

    @Test
    void testRandomObject() {
        EasyRandomParameters parameters = new EasyRandomParameters();
        parameters.setObjectFactory(new ObjenesisObjectFactory());
        Object random = new EasyRandom(parameters).nextObject(Object[].class);
        System.out.println("random = " + random);
    }

}
