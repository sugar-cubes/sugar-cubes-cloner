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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Implementation of {@link JdkConfiguration} for JDK 9+.
 *
 * @author Maxim Butov
 */
class JdkConfigurationImpl$Jdk9 extends JdkConfigurationImpl$Jdk8 {

    public JdkConfigurationImpl$Jdk9() {
        systemWideSingletons.addAll(Arrays.asList(List.of(), Set.of(), Map.of()));
    }

    @Override
    protected UnsafeBridge getUnsafeImpl() {
        try {
            return new JdkInternalMiscUnsafeBridge();
        }
        catch (Throwable e) {
            // fallback to the classics
            return super.getUnsafeImpl();
        }
    }

    private final Instrumentation instrumentation = ClonerAgent.getInstrumentation();
    private final Module clonerModule = JdkConfigurationImpl$Jdk9.class.getModule();
    private final Set<Class<?>> accessible = Collections.newSetFromMap(new WeakHashMap<>());

    @Override
    public void makeAccessible(Class<?> type) {
        if (instrumentation != null && accessible.add(type)) {
            Module module = type.getModule();
            if (module != clonerModule) {
                String packageName = type.getPackageName();
                if (!module.isOpen(packageName, clonerModule)) {
                    instrumentation.redefineModule(module, Set.of(), Map.of(),
                        Map.of(packageName, Set.of(clonerModule)), Set.of(), Map.of());
                }
            }
        }
    }

}
