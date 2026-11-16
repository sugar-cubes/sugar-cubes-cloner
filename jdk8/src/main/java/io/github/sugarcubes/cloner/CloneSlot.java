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

import java.util.concurrent.Callable;

/**
 * State of copying of one original object.
 *
 * <p>A slot goes through the states: new, creating (a thread copies the object), created (the clone exists, for a
 * two-phase copier the second phase is waiting to be claimed), completing (a thread runs the second phase) and
 * completed (the own state of the clone is copied). A copier which throws leaves the slot failed.</p>
 *
 * @author Maxim Butov
 */
final class CloneSlot {

    private Object clone;

    private boolean completed;

    private boolean failed;

    private Callable<?> secondPhase;

    /**
     * Thread which creates or completes the clone, {@code null} if nobody does.
     */
    private volatile Thread busyThread;

    /**
     * Creates a slot for an object which is not copied yet.
     */
    CloneSlot() {
    }

    /**
     * Creates a completed slot for a predefined clone.
     *
     * @param clone clone
     */
    CloneSlot(Object clone) {
        this.clone = clone;
        this.completed = true;
    }

    synchronized Object getClone() {
        return clone;
    }

    synchronized boolean isCreated() {
        return clone != null;
    }

    synchronized boolean isCompleted() {
        return completed;
    }

    synchronized boolean isFailed() {
        return failed;
    }

    Thread getBusyThread() {
        return busyThread;
    }

    /**
     * Makes the current thread the copier of the object if nobody has started copying it.
     *
     * @return {@code true} if the current thread must copy the object
     */
    synchronized boolean start() {
        if (clone != null || failed || busyThread != null) {
            return false;
        }
        busyThread = Thread.currentThread();
        return true;
    }

    /**
     * Registers the clone. A single-phase copier may register it before it returns.
     *
     * @param clone clone
     * @return {@code false} if the clone is already registered
     */
    synchronized boolean register(Object clone) {
        if (this.clone != null) {
            return false;
        }
        this.clone = clone;
        notifyAll();
        return true;
    }

    /**
     * Registers the clone created by the first phase and the second phase which completes it.
     *
     * @param clone clone
     * @param secondPhase second phase
     */
    synchronized void created(Object clone, Callable<?> secondPhase) {
        this.clone = clone;
        this.secondPhase = secondPhase;
        this.busyThread = null;
        notifyAll();
    }

    /**
     * Takes the second phase to run it in the current thread.
     *
     * @return second phase or {@code null} if it is already taken or there is no second phase
     */
    synchronized Callable<?> claimSecondPhase() {
        Callable<?> task = secondPhase;
        if (task != null) {
            secondPhase = null;
            busyThread = Thread.currentThread();
        }
        return task;
    }

    /**
     * Marks the clone completed.
     */
    synchronized void completed() {
        completed = true;
        busyThread = null;
        notifyAll();
    }

    /**
     * Releases the slot after the current thread has finished its work, marks it failed if the work is not done.
     *
     * @param done whether the work is done
     */
    synchronized void release(boolean done) {
        if (busyThread == Thread.currentThread()) {
            busyThread = null;
            if (!done) {
                failed = true;
            }
            notifyAll();
        }
    }

}
