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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JDK version enum.
 *
 * @author Maxim Butov
 */
enum JdkVersion {

    V8(8), V9(9), V11(11), V17(17), V21(21);

    final int version;

    JdkVersion(int version) {
        this.version = version;
    }

    static final JdkVersion CURRENT;

    static {
        if (ReflectionUtils.isClassAvailable("java.lang.MatchException")) {
            CURRENT = V21;
        }
        else if (ReflectionUtils.isClassAvailable("java.time.InstantSource")) {
            CURRENT = V17;
        }
        else if (ReflectionUtils.isClassAvailable("java.lang.invoke.ConstantBootstraps")) {
            CURRENT = V11;
        }
        else if (ReflectionUtils.isClassAvailable("java.lang.Module")) {
            CURRENT = V9;
        }
        else {
            CURRENT = V8;
        }
    }

    static final JdkConfiguration CONFIGURATION;

    static {
        JdkConfiguration configuration = null;

        int currentVersion = CURRENT.version;
        List<JdkVersion> versions = Arrays.stream(JdkVersion.values())
            .filter(v -> v.version <= currentVersion)
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());
        for (JdkVersion jdkVersion : versions) {
            String configurationClassName =
                String.format("%s.Jdk%dConfigurationImpl", JdkVersion.class.getPackage().getName(), jdkVersion.version);
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
