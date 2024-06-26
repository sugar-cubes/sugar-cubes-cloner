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

/**
 * Implementation of {@link JdkConfiguration} for JDK 16+.
 *
 * @author Maxim Butov
 */
class JdkConfigurationImpl$Jdk16 extends JdkConfigurationImpl$Jdk15 {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ObjectCopier<?> getCopier(Class<?> type) {
        return type.isRecord() ? new RecordCopier(type) : super.getCopier(type);
    }

}
