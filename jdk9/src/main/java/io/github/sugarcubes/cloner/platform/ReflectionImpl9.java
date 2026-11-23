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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Member;

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.reflection.ReflectionImpl8;

/**
 * Reflection for JDK 9+: before making a member accessible opens its package to the cloner.
 *
 * @author Maxim Butov
 */
class ReflectionImpl9 extends ReflectionImpl8 {

    private final TypeOpener opener;

    /**
     * Constructor.
     *
     * @param opener opener of packages for deep reflection
     */
    ReflectionImpl9(TypeOpener opener) {
        this.opener = opener;
    }

    @Override
    protected <T extends AccessibleObject & Member> T makeAccessibleImpl(T member) {
        opener.open(member.getDeclaringClass());
        try {
            return super.makeAccessibleImpl(member);
        }
        catch (InaccessibleObjectException e) {
            throw new ClonerException(String.format("Cannot make %s accessible. Open its package to the cloner with "
                + "--add-opens JVM option or use a toolset with module opening.", member), e);
        }
    }

    @Override
    protected <T extends AccessibleObject & Member> boolean tryMakeAccessibleImpl(T member) {
        opener.open(member.getDeclaringClass());
        return member.trySetAccessible();
    }

}
