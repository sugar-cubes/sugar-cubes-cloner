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
import java.util.Map;
import java.util.Set;

/**
 * todo
 */
public class InstrumentationModuleOpenerImpl extends AbstractModuleOpener {

    private final Instrumentation instrumentation;

    public InstrumentationModuleOpenerImpl(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Override
    protected void openModule(Module module, String packageName, Module thisModule) throws Exception {
        Map<String, Set<Module>> extra = Map.of(packageName, Set.of(thisModule));
        instrumentation.redefineModule(module, Set.of(), extra, extra, Set.of(), Map.of());
    }

}
