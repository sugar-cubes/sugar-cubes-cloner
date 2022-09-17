/*
 * Copyright 2017-2022 the original author or authors.
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

/**
 * Type copy policy based on annotations.
 *
 * @author Maxim Butov
 */
public class AnnotatedTypeCopyPolicy implements CopyPolicy<Class<?>> {

    @Override
    public CopyAction getAction(Class<?> type) {
        TypePolicy annotation = type.getDeclaredAnnotation(TypePolicy.class);
        if (annotation != null) {
            return annotation.value();
        }
        for (Class<?> t = type; (t = t.getSuperclass()) != null; ) {
            annotation = t.getDeclaredAnnotation(TypePolicy.class);
            if (annotation != null && annotation.applyToSubtypes()) {
                return annotation.value();
            }
        }
        return CopyAction.DEFAULT;
    }

}
