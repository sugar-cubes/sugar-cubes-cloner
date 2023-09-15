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

import java.util.function.BooleanSupplier;

import static io.github.sugarcubes.cloner.ClassUtils.classForName;
import static io.github.sugarcubes.cloner.ClassUtils.isClassAvailable;
import static io.github.sugarcubes.cloner.ClassUtils.isMethodAvailable;

/**
 * JDK version enum.
 *
 * @author Maxim Butov
 */
enum JdkVersion {

    V8(8, "java.lang.FunctionalInterface"),
    V9(9, "java.lang.Module"),
    V11(11, "java.lang.invoke.ConstantBootstraps"),
    V15(15, "java.security.interfaces.EdECKey"),
    V16(16, "java.net.UnixDomainSocketAddress"),
    V17(17, "java.util.HexFormat"),
    V21(21, "java.lang.ScopedValue");

    final int version;
    final BooleanSupplier applicable;

    JdkVersion(int version, BooleanSupplier applicable) {
        this.version = version;
        this.applicable = applicable;
    }

    JdkVersion(int version, String availableClass) {
        this(version, () -> isClassAvailable(availableClass));
    }

    JdkVersion(int version, String type, String methodName, Class<?>... parameterTypes) {
        this(version, () -> isClassAvailable(type) && isMethodAvailable(classForName(type), methodName, parameterTypes));
    }

    boolean applicable() {
        return applicable.getAsBoolean();
    }

    static final JdkVersion MINIMAL = V8;

    static final JdkVersion CURRENT;

    static {
        JdkVersion version = null;
        for (JdkVersion v : values()) {
            if (v.applicable()) {
                version = v;
            }
            else {
                break;
            }
        }
        if (version == null) {
            throw Checks.mustNotHappen();
        }
        CURRENT = version;
    }

}
