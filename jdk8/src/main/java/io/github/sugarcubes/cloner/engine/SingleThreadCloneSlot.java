/*
 * Copyright 2017-2026 the original author or authors.
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
package io.github.sugarcubes.cloner.engine;

import java.util.function.Predicate;

import io.github.sugarcubes.cloner.util.Checks;

/**
 * Not synchronized slot for single-thread contexts, where nobody waits for another thread. Also used for predefined
 * clones, which are completed when created and never change.
 *
 * @author Maxim Butov
 */
final class SingleThreadCloneSlot extends CloneSlot {

    /**
     * Creates a slot for an object which is not copied yet.
     *
     * @param original original object
     */
    SingleThreadCloneSlot(Object original) {
        super(original);
    }

    /**
     * Creates a completed slot for a predefined clone.
     *
     * @param original original object
     * @param clone clone
     */
    SingleThreadCloneSlot(Object original, Object clone) {
        super(original, clone);
    }

    @Override
    Thread getBusyThread() {
        // only the current thread uses the slot
        return isBusy() ? Thread.currentThread() : null;
    }

    @Override
    boolean isBusyByCurrentThread() {
        return isBusy();
    }

    @Override
    protected void signalChange() {
        // nobody waits
    }

    @Override
    void awaitChange(Predicate<CloneSlot> ready, long timeoutMillis) {
        // in a single thread everything not ready is waited for by the current thread itself, that is a cycle
        throw Checks.mustNotHappen();
    }

}
