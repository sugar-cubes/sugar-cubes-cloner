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
 * Copier which always returns {@code null}.
 *
 * @author Maxim Butov
 */
public final class NullCopier<T> implements TrivialCopier<T> {

    /**
     * Do not create outside of package. One should use {@link ObjectCopier#NULL}.
     */
    NullCopier() {
    }

    @Override
    public T copy(T original, CopyContext context) throws Exception {
        return null;
    }

}
