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

import java.util.Optional;

import io.github.sugarcubes.cloner.generation.BytecodeBackend;

/**
 * {@link JdkPlatform} of JDK 24+: classes are written by the ClassFile API, no library is needed.
 *
 * @author Maxim Butov
 */
class JdkPlatformImpl$Jdk24 extends JdkPlatformImpl$Jdk16 {

    @Override
    public Optional<BytecodeBackend> classFileBackend() {
        return Optional.of(new ClassFileBytecodeBackend());
    }

}
