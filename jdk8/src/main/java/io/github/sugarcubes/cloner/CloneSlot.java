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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

/**
 * State of copying of one original object.
 *
 * <p>A slot goes through the states: new, creating (a thread copies the object), created (the clone exists, for a
 * two-phase copier the second phase is waiting to be claimed), completing (a thread runs the second phase) and
 * completed (the own state of the clone is copied). A copier which throws leaves the slot failed.</p>
 *
 * <p>The second phase may meet an object whose clone is being created right now (a reference cycle through a
 * single-phase copier, or another thread). Then it subscribes to that object's slot instead of waiting, and the
 * slot stays not completed until all such holes are filled.</p>
 *
 * @author Maxim Butov
 */
final class CloneSlot {

    private static final CloneSlot[] NO_SLOTS = {};

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Signalled on every change of the state.
     */
    private final Condition changed = lock.newCondition();

    private Object clone;

    private boolean completed;

    private boolean failed;

    private Callable<?> secondPhase;

    private boolean secondPhaseDone;

    /**
     * Subscriptions of other slots waiting for the clone of this slot.
     */
    private List<Subscription> subscriptions;

    /**
     * Slots whose clones this slot waits for, one entry per hole.
     */
    private volatile CloneSlot[] blockers = NO_SLOTS;

    /**
     * Thread which creates or completes the clone, {@code null} if nobody does.
     */
    private volatile Thread busyThread;

    /**
     * Subscription of a slot to the clone of another slot.
     */
    static final class Subscription {

        final CloneSlot owner;

        final CopyContext.CloneSetter<Object> setter;

        Subscription(CloneSlot owner, CopyContext.CloneSetter<Object> setter) {
            this.owner = owner;
            this.setter = setter;
        }

    }

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

    /**
     * Tells whether the second phase is waiting to be claimed.
     *
     * @return {@code true} if the second phase can be claimed
     */
    boolean isClaimable() {
        lock.lock();
        try {
            return secondPhase != null;
        }
        finally {
            lock.unlock();
        }
    }

    Thread getBusyThread() {
        return busyThread;
    }

    CloneSlot[] getBlockers() {
        return blockers;
    }

    /**
     * Waits for a change of the state unless the slot is already ready, failed or claimable. The wait is limited,
     * so the caller can check for a reference cycle again.
     *
     * @param ready readiness condition
     * @param timeoutMillis maximal waiting time
     * @throws InterruptedException if the current thread is interrupted
     */
    void awaitChange(Predicate<CloneSlot> ready, long timeoutMillis) throws InterruptedException {
        lock.lock();
        try {
            if (!ready.test(this) && !failed && secondPhase == null) {
                changed.await(timeoutMillis, TimeUnit.MILLISECONDS);
            }
        }
        finally {
            lock.unlock();
        }
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
     * @return subscriptions to notify, or {@code null} if the clone is already registered
     */
    List<Subscription> register(Object clone) {
        lock.lock();
        try {
            if (this.clone != null) {
                return null;
            }
            this.clone = clone;
            changed.signalAll();
            return takeSubscriptions();
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
     * @return subscriptions to notify
     */
    List<Subscription> created(Object clone, Callable<?> secondPhase) {
        lock.lock();
        try {
            this.clone = clone;
            this.secondPhase = secondPhase;
            this.busyThread = null;
            changed.signalAll();
            return takeSubscriptions();
        }
        finally {
            lock.unlock();
        }
    }

    private List<Subscription> takeSubscriptions() {
        List<Subscription> taken = subscriptions != null ? subscriptions : Collections.emptyList();
        subscriptions = null;
        return taken;
    }

    /**
     * Subscribes the owner slot to the clone of this slot.
     *
     * @param subscription subscription
     * @return the clone if it already exists (the subscription is not stored then), otherwise {@code null}
     * @throws ClonerException if copying of the object has failed
     */
    Object subscribe(Subscription subscription) {
        lock.lock();
        try {
            if (clone != null) {
                return clone;
            }
            if (failed) {
                throw new ClonerException("Copying of a referenced object failed.");
            }
            if (subscriptions == null) {
                subscriptions = new ArrayList<>(1);
            }
            subscriptions.add(subscription);
            return null;
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
     * Marks the clone of a single-phase copier completed.
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
     * Marks the second phase done; the clone is completed when all holes are filled.
     */
    void secondPhaseDone() {
        lock.lock();
        try {
            secondPhaseDone = true;
            busyThread = null;
            completeIfNoHoles();
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Registers a hole: this slot waits for the clone of the blocker.
     *
     * @param blocker slot whose clone is waited for
     */
    void addHole(CloneSlot blocker) {
        lock.lock();
        try {
            CloneSlot[] current = blockers;
            CloneSlot[] updated = Arrays.copyOf(current, current.length + 1);
            updated[current.length] = blocker;
            blockers = updated;
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Removes a hole after the clone of the blocker is set.
     *
     * @param blocker slot whose clone has been set
     */
    void holeFilled(CloneSlot blocker) {
        lock.lock();
        try {
            CloneSlot[] current = blockers;
            List<CloneSlot> updated = new ArrayList<>(Arrays.asList(current));
            updated.remove(blocker);
            blockers = updated.toArray(NO_SLOTS);
            completeIfNoHoles();
        }
        finally {
            lock.unlock();
        }
    }

    private void completeIfNoHoles() {
        if (secondPhaseDone && blockers.length == 0) {
            completed = true;
        }
        changed.signalAll();
    }

    /**
     * Marks the slot failed, e.g. because an object it waits for has failed.
     */
    void fail() {
        lock.lock();
        try {
            failed = true;
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
     * @return subscriptions which will never get the clone, if the slot has failed
     */
    List<Subscription> release(boolean done) {
        lock.lock();
        try {
            if (busyThread != Thread.currentThread()) {
                return Collections.emptyList();
            }
            busyThread = null;
            if (done) {
                changed.signalAll();
                return Collections.emptyList();
            }
            failed = true;
            changed.signalAll();
            return takeSubscriptions();
        }
        finally {
            lock.unlock();
        }
    }

}
