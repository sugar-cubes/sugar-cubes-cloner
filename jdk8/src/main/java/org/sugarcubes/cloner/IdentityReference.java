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

/**
 * Reference to an object (or null reference) to be used as a map key in order to force a map act as identity map.
 *
 * @author Maxim Butov
 */
public final class IdentityReference<K> {

    private final K reference;
    private final int hash;

    IdentityReference(K reference) {
        this.reference = reference;
        this.hash = System.identityHashCode(reference);
    }

    public K getReference() {
        return reference;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IdentityReference && ((IdentityReference<?>) obj).reference == reference;
    }

    @Override
    public int hashCode() {
        return hash;
    }

}
