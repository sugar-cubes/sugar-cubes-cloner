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

import static org.sugarcubes.cloner.ReflectionUtils.isClassAvailable;

/**
 * JDK configuration loader and holder.
 *
 * @author Maxim Butov
 */
class JdkConfigurationHolder {

    static final JdkConfiguration CONFIGURATION;

    static {
        JdkConfiguration configuration = null;

        String packageName = JdkConfigurationHolder.class.getPackage().getName();
        for (int version = JdkVersion.CURRENT.version; version >= JdkVersion.MINIMAL.version; version--) {
            String configurationClassName = String.format("%s.Jdk%dConfigurationImpl", packageName, version);
            if (isClassAvailable(configurationClassName)) {
                configuration = ReflectionUtils.newInstance(configurationClassName);
                break;
            }
        }

        if (configuration == null) {
            throw Checks.mustNotHappen();
        }

        CONFIGURATION = configuration;
    }

}
