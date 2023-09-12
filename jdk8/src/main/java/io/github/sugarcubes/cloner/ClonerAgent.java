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

import java.lang.instrument.Instrumentation;

/**
 * Java agent which gives access to {@link Instrumentation} for cloner module.
 *
 * @author Maxim Butov
 */
class ClonerAgent {

    private static Instrumentation instrumentation;

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        ClonerAgent.instrumentation = instrumentation;
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        ClonerAgent.instrumentation = instrumentation;
    }

    static Instrumentation getInstrumentation() {
        return instrumentation;
    }

}
