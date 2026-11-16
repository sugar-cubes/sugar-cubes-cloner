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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

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

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Signalled on every change of the state.
     */
    private final Condition changed = lock.newCondition();

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

    /**
     * Acquires the lock of the slot, e.g. to check the state and wait for its change atomically.
     */
    void lock() {
        lock.lock();
    }

    /**
     * Releases the lock of the slot.
     */
    void unlock() {
        lock.unlock();
    }

    /**
     * Waits for a change of the state. The current thread must hold the lock.
     *
     * @throws InterruptedException if the current thread is interrupted
     */
    void awaitChange() throws InterruptedException {
        changed.await();
    }

    Object getClone() {
        lock.lock();
        try {
            return clone;
        }
        finally {
            lock.unlock();
        }
    }

    boolean isCreated() {
        return getClone() != null;
    }

    boolean isCompleted() {
        lock.lock();
        try {
            return completed;
        }
        finally {
            lock.unlock();
        }
    }

    boolean isFailed() {
        lock.lock();
        try {
            return failed;
        }
        finally {
            lock.unlock();
        }
    }

    Thread getBusyThread() {
        return busyThread;
    }

    /**
     * Makes the current thread the copier of the object if nobody has started copying it.
     *
     * @return {@code true} if the current thread must copy the object
     */
    boolean start() {
        lock.lock();
        try {
            if (clone != null || failed || busyThread != null) {
                return false;
            }
            busyThread = Thread.currentThread();
            return true;
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Registers the clone. A single-phase copier may register it before it returns.
     *
     * @param clone clone
     * @return {@code false} if the clone is already registered
     */
    boolean register(Object clone) {
        lock.lock();
        try {
            if (this.clone != null) {
                return false;
            }
            this.clone = clone;
            changed.signalAll();
            return true;
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Registers the clone created by the first phase and the second phase which completes it.
     *
     * @param clone clone
     * @param secondPhase second phase
     */
    void created(Object clone, Callable<?> secondPhase) {
        lock.lock();
        try {
            this.clone = clone;
            this.secondPhase = secondPhase;
            this.busyThread = null;
            changed.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Takes the second phase to run it in the current thread.
     *
     * @return second phase or {@code null} if it is already taken or there is no second phase
     */
    Callable<?> claimSecondPhase() {
        lock.lock();
        try {
            Callable<?> task = secondPhase;
            if (task != null) {
                secondPhase = null;
                busyThread = Thread.currentThread();
            }
            return task;
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Marks the clone completed.
     */
    void completed() {
        lock.lock();
        try {
            completed = true;
            busyThread = null;
            changed.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Releases the slot after the current thread has finished its work, marks it failed if the work is not done.
     *
     * @param done whether the work is done
     */
    void release(boolean done) {
        lock.lock();
        try {
            if (busyThread == Thread.currentThread()) {
                busyThread = null;
                if (!done) {
                    failed = true;
                }
                changed.signalAll();
            }
        }
        finally {
            lock.unlock();
        }
    }

}
