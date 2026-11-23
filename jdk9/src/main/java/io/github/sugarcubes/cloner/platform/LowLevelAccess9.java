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
package io.github.sugarcubes.cloner.platform;

import java.lang.instrument.Instrumentation;
import java.util.Optional;
import java.util.function.Supplier;

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.ModuleOpening;
import io.github.sugarcubes.cloner.reflection.ClonerAgent;
import io.github.sugarcubes.cloner.spi.FieldAdapterFactory;
import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.unsafe.UnsafeBridge;
import io.github.sugarcubes.cloner.util.Checks;
import io.github.sugarcubes.cloner.util.NarcissusUtils;

/**
 * {@link LowLevelAccess} of JDK 9+: reflection opening modules, {@code VarHandle}, {@code jdk.internal.misc.Unsafe}.
 *
 * @author Maxim Butov
 */
class LowLevelAccess9 extends LowLevelAccess8 {

    @Override
    public Reflection reflection(ModuleOpening opening, Supplier<UnsafeBridge> unsafe) {
        return new ReflectionImpl9(typeOpener(opening, unsafe));
    }

    private static TypeOpener typeOpener(ModuleOpening opening, Supplier<UnsafeBridge> unsafe) {
        switch (opening) {
            case NONE:
                return TypeOpener.NOOP;
            case UNSAFE:
                return new UnsafeModuleOpenerImpl(unsafe.get());
            case NARCISSUS:
                if (!NarcissusUtils.isNarcissusAvailable()) {
                    throw new ClonerException("Narcissus is not available: put it on the classpath.");
                }
                return new NarcissusModuleOpenerImpl();
            case INSTRUMENTATION:
                Instrumentation instrumentation = ClonerAgent.getInstrumentation();
                if (instrumentation == null) {
                    throw new ClonerException("Instrumentation is not available: start the JVM with -javaagent "
                        + "pointing to the cloner jar.");
                }
                return new InstrumentationModuleOpenerImpl(instrumentation);
            default:
                throw Checks.mustNotHappen();
        }
    }

    @Override
    public Optional<FieldAdapterFactory> varHandleFieldAdapters() {
        try {
            return Optional.of(VarHandleFieldAdapterImpl.FACTORY);
        }
        catch (LinkageError e) {
            throw new ClonerException("VarHandle field access needs the trusted lookup: open java.base/java.lang.invoke "
                + "to the cloner with --add-opens JVM option.", e);
        }
    }

    @Override
    public UnsafeBridge unsafeBridge(Reflection reflection) {
        try {
            return newJdkInternalMiscUnsafeBridge();
        }
        catch (LinkageError | ClonerException e) {
            // IllegalAccessError when jdk.internal.misc is not exported, fallback to sun.misc.Unsafe
            return super.unsafeBridge(reflection);
        }
    }

    /**
     * Creates the bridge to {@code jdk.internal.misc.Unsafe} of this JDK version.
     *
     * @return Unsafe bridge
     */
    UnsafeBridge newJdkInternalMiscUnsafeBridge() {
        return new JdkInternalMiscUnsafeBridge9();
    }

}
