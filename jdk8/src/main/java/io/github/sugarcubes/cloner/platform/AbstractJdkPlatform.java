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

/**
 * Platform combining its parts; {@code JdkPlatformImpl} of every JDK version chooses the versions of the parts.
 *
 * @author Maxim Butov
 */
abstract class AbstractJdkPlatform implements JdkPlatform {

    private final TypeKinds typeKinds;

    private final TypeModels typeModels;

    private final LowLevelAccess lowLevelAccess;

    private final ClassDefiner classDefiner;

    /**
     * Creates the platform.
     *
     * @param typeKinds type kinds
     * @param typeModels type models
     * @param lowLevelAccess low-level access
     * @param classDefiner class definer
     */
    AbstractJdkPlatform(TypeKinds typeKinds, TypeModels typeModels, LowLevelAccess lowLevelAccess,
        ClassDefiner classDefiner) {
        this.typeKinds = typeKinds;
        this.typeModels = typeModels;
        this.lowLevelAccess = lowLevelAccess;
        this.classDefiner = classDefiner;
    }

    @Override
    public TypeKinds typeKinds() {
        return typeKinds;
    }

    @Override
    public TypeModels typeModels() {
        return typeModels;
    }

    @Override
    public LowLevelAccess lowLevelAccess() {
        return lowLevelAccess;
    }

    @Override
    public ClassDefiner classDefiner() {
        return classDefiner;
    }

}
