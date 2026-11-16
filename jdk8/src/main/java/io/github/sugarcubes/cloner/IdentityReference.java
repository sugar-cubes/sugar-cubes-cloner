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
 * Reference to an object (or null reference) to be used as a map key in order to force a map act as identity map.
 *
 * @author Maxim Butov
 */
public final class IdentityReference<K> {

    /**
     * Object reference.
     */
    private final K object;

    /**
     * System identity hash code of {@link #object}.
     */
    private final int hash;

    /**
     * Creates identity reference for the object.
     *
     * @param object object
     */
    public IdentityReference(K object) {
        this.object = object;
        this.hash = System.identityHashCode(object);
    }

    /**
     * Returns the object.
     *
     * @return object
     */
    public K getObject() {
        return object;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IdentityReference && ((IdentityReference<?>) obj).object == this.object;
    }

    @Override
    public int hashCode() {
        return hash;
    }

}
