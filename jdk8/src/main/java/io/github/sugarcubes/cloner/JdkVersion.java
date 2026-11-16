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

import java.util.Arrays;
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

    // versions must be reverse ordered

    V21(21, "java.lang.ScopedValue"),
    V17(17, "java.util.HexFormat"),
    V16(16, "java.net.UnixDomainSocketAddress"),
    V15(15, "java.security.interfaces.EdECKey"),
    V11(11, "java.lang.invoke.ConstantBootstraps"),
    V9(9, "java.lang.Module"),
    V8(8, "java.lang.FunctionalInterface");

    final int version;
    final BooleanSupplier applicable;

    JdkVersion(int version, String availableClass) {
        this.version = version;
        this.applicable = () -> isClassAvailable(availableClass);
    }

    JdkVersion(int version, String type, String methodName, Class<?>... parameterTypes) {
        this.version = version;
        this.applicable = () -> isClassAvailable(type) && isMethodAvailable(classForName(type), methodName, parameterTypes);
    }

    boolean applicable() {
        return applicable.getAsBoolean();
    }

    static final JdkVersion MINIMAL = V8;

    static final JdkVersion CURRENT = Arrays.stream(values())
        .filter(JdkVersion::applicable)
        .findFirst()
        .orElseThrow(Checks::mustNotHappen);

}
