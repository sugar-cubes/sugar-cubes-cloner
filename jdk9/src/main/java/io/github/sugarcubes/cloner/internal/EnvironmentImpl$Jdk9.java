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
package io.github.sugarcubes.cloner.internal;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

import io.github.sugarcubes.cloner.ClassUtils;
import sun.misc.Unsafe;

/**
 * todo
 */
public class EnvironmentImpl$Jdk9 extends EnvironmentImpl$Jdk8 {

    private static final boolean NARCISSUS = ClassUtils.isClassAvailable("io.github.toolfactory.narcissus.Narcissus");

    private final List<TypeOpener> openers = new ArrayList<>();

    public EnvironmentImpl$Jdk9() {

        Instrumentation instrumentation = ClonerAgent.getInstrumentation();
        if (instrumentation != null) {
            openers.add(new InstrumentationModuleOpenerImpl(instrumentation));
        }
        if (NARCISSUS) {
            openers.add(new NarcissusModuleOpenerImpl());
        }
        openers.add(TypeOpener.NOOP);

    }

    @Override
    public TypeOpener getOpener() {
        Instrumentation instrumentation = ClonerAgent.getInstrumentation();
        if (instrumentation != null) {
            return new InstrumentationModuleOpenerImpl(instrumentation);
        }
        if (NARCISSUS) {
            return new NarcissusModuleOpenerImpl();
        }
        return TypeOpener.NOOP;
    }

    @Override
    public Unsafe getUnsafe() {
        return null;
    }

}
