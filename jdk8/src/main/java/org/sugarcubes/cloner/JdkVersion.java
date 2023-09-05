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
package org.sugarcubes.cloner;

import java.util.Arrays;

/**
 * JDK version enum.
 *
 * @author Maxim Butov
 */
enum JdkVersion {

    // versions must be reverse ordered

    V21(21, "java.lang.ScopedValue"),
    V17(17, "java.util.HexFormat"),
    V15(15, "java.security.interfaces.EdECKey"),
    V11(11, "java.lang.invoke.ConstantBootstraps"),
    V9(9, "java.lang.Module"),
    V8(8, "java.lang.FunctionalInterface");

    final int version;
    final String availableClass;

    JdkVersion(int version, String availableClass) {
        this.version = version;
        this.availableClass = availableClass;
    }

    boolean applicable() {
        return ReflectionUtils.isClassAvailable(availableClass);
    }

    String configurationClassName() {
        return String.format("%s.Jdk%dConfigurationImpl", JdkVersion.class.getPackage().getName(), version);
    }

    static final JdkVersion CURRENT = Arrays.stream(values())
        .filter(JdkVersion::applicable)
        .findFirst()
        .orElseThrow(Checks::mustNotHappen);

    static final JdkConfiguration CONFIGURATION;

    static {
        JdkConfiguration configuration = null;

        JdkVersion[] versions = values();
        for (int k = Arrays.asList(versions).indexOf(CURRENT); k < versions.length; k++) {
            String configurationClassName = versions[k].configurationClassName();
            if (ReflectionUtils.isClassAvailable(configurationClassName)) {
                configuration = ReflectionUtils.newInstance(configurationClassName);
                break;
            }
        }

        if (configuration == null) {
            configuration = new Jdk8ConfigurationImpl();
        }

        CONFIGURATION = configuration;
    }

}
