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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Java agent which opens modules (allows reflection access) for cloner module.
 *
 * @author Maxim Butov
 */
class ModuleOpeningAgent {

    static String asPath(String packageOrClassName) {
        return packageOrClassName.replace('.', '/');
    }

    static final String CLONER_PREFIX = asPath(ModuleOpeningAgent.class.getPackageName()) + '/';
    static final String AGENT_PREFIX = asPath(ModuleOpeningAgent.class.getName());

    static boolean isClonerModuleClass(String className) {
        return className.startsWith(CLONER_PREFIX)
            && !className.startsWith(AGENT_PREFIX)
            && className.indexOf('/', CLONER_PREFIX.length()) < 0;
    }

    static class ClassLoaderModuleOpener {

        final Set<Module> modules = Collections.newSetFromMap(new WeakHashMap<>());
        Reference<Module> clonerModule;

        ClassLoaderModuleOpener() {
            modules.add(String.class.getModule());
        }

        void check(Instrumentation instrumentation, Module module, boolean isClonerModule) {
            Module clonerModule = this.clonerModule != null ? this.clonerModule.get() : null;
            if (clonerModule == null) {
                if (isClonerModule) {
                    this.clonerModule = new WeakReference<>(module);
                    modules.remove(module);
                    for (Module m : modules) {
                        open(instrumentation, m, module);
                    }
                }
                else {
                    modules.add(module);
                }
            }
            else if (module != clonerModule && modules.add(module)) {
                open(instrumentation, module, clonerModule);
            }
        }

        void open(Instrumentation instrumentation, Module module, Module clonerModule) {
            Set<Module> clonerModuleAsSet = Set.of(clonerModule);
            instrumentation.redefineModule(module, Set.of(), Map.of(),
                module.getPackages().stream().collect(Collectors.toMap(Function.identity(), name -> clonerModuleAsSet)),
                Set.of(), Map.of());
        }

    }

    static class ModuleOpeningTransformer implements ClassFileTransformer {

        final Instrumentation instrumentation;
        final Map<ClassLoader, ClassLoaderModuleOpener> openers = new WeakHashMap<>();

        public ModuleOpeningTransformer(Instrumentation instrumentation) {
            this.instrumentation = instrumentation;
        }

        @Override
        public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

            if (loader != null) {
                ClassLoaderModuleOpener opener = openers.get(loader);
                if (opener == null) {
                    synchronized (this) {
                        opener = openers.computeIfAbsent(loader, key -> new ClassLoaderModuleOpener());
                    }
                }
                opener.check(instrumentation, module, isClonerModuleClass(className));
            }
            else {
                List<ClassLoaderModuleOpener> toCheck = new ArrayList<>(openers.values());
                toCheck.forEach(opener -> opener.check(instrumentation, module, false));
            }

            return null;
        }

    }

    static void install(Instrumentation instrumentation) {
        instrumentation.addTransformer(new ModuleOpeningTransformer(instrumentation));
    }

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        install(instrumentation);
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        install(instrumentation);
    }

}
