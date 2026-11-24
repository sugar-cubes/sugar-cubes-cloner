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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.CopyContext;
import io.github.sugarcubes.cloner.TwoPhaseObjectCopier;

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
 * <p>The state machine is not synchronized; {@link SingleThreadCloneSlot} is used by single-thread contexts and for
 * predefined clones, {@link ConcurrentCloneSlot} synchronizes it for the parallel context.</p>
 *
 * @author Maxim Butov
 */
abstract class CloneSlot {

    private static final CloneSlot[] NO_SLOTS = {};

    private static final int COMPLETED = 1;

    private static final int FAILED = 2;

    /**
     * The second phase is waiting to be claimed.
     */
    private static final int CLAIMABLE = 4;

    private static final int SECOND_PHASE_DONE = 8;

    /**
     * A thread creates or completes the clone.
     */
    private static final int BUSY = 16;

    /**
     * The original object, the key of the slot in a {@link SlotMap}; final, so a slot published by a race is seen
     * with its original.
     */
    final Object original;

    private Object clone;

    private int state;

    /**
     * Copier of the second phase, set when the first phase is done.
     */
    private TwoPhaseObjectCopier<Object> copier;

    /**
     * Data needed only for reference cycles and waiting, created on demand to keep slots small.
     */
    private Rare rare;

    /**
     * Rarely needed data of a slot.
     */
    private static final class Rare {

        /**
         * Subscriptions of other slots waiting for the clone of this slot.
         */
        private List<Subscription> subscriptions;

        /**
         * Slots whose clones this slot waits for, one entry per hole.
         */
        private CloneSlot[] blockers = NO_SLOTS;

    }

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
     *
     * @param original original object
     */
    CloneSlot(Object original) {
        this.original = original;
    }

    /**
     * Creates a completed slot for a predefined clone.
     *
     * @param original original object
     * @param clone clone
     */
    CloneSlot(Object original, Object clone) {
        this.original = original;
        this.clone = clone;
        this.state = COMPLETED;
    }

    /**
     * Notifies waiting threads about a change of the state.
     */
    protected abstract void signalChange();

    /**
     * Waits for a change of the state unless the slot is already ready, failed or claimable. The wait is limited,
     * so the caller can check for a reference cycle again.
     *
     * @param ready readiness condition
     * @param timeoutMillis maximal waiting time
     * @throws InterruptedException if the current thread is interrupted
     */
    abstract void awaitChange(Predicate<CloneSlot> ready, long timeoutMillis) throws InterruptedException;

    /**
     * Tells whether waiting is needed. Must be called by {@link #awaitChange(Predicate, long)} implementations.
     *
     * @param ready readiness condition
     * @return {@code true} if the slot is neither ready, nor failed, nor claimable
     */
    final boolean mustWait(Predicate<CloneSlot> ready) {
        return !ready.test(this) && !is(FAILED) && !is(CLAIMABLE);
    }

    private boolean is(int flag) {
        return (state & flag) != 0;
    }

    private Rare rare() {
        if (rare == null) {
            rare = new Rare();
        }
        return rare;
    }

    Object getClone() {
        return clone;
    }

    boolean isCreated() {
        return getClone() != null;
    }

    boolean isCompleted() {
        return is(COMPLETED);
    }

    boolean isFailed() {
        return is(FAILED);
    }

    boolean isClaimable() {
        return is(CLAIMABLE);
    }

    boolean isBusy() {
        return is(BUSY);
    }

    /**
     * Returns the thread which creates or completes the clone.
     *
     * @return busy thread or {@code null} if nobody is busy with the slot
     */
    abstract Thread getBusyThread();

    /**
     * Tells whether the current thread creates or completes the clone.
     *
     * @return {@code true} if the current thread is busy with the slot
     */
    abstract boolean isBusyByCurrentThread();

    /**
     * Marks the current thread busy with the slot or not; a slot used by several threads remembers the thread.
     *
     * @param busy whether the current thread is busy with the slot
     */
    void setBusy(boolean busy) {
        state = busy ? state | BUSY : state & ~BUSY;
    }

    CloneSlot[] getBlockers() {
        return rare != null ? rare.blockers : NO_SLOTS;
    }

    TwoPhaseObjectCopier<Object> getCopier() {
        return copier;
    }

    /**
     * Makes the current thread the copier of the object if nobody has started copying it.
     *
     * @return {@code true} if the current thread must copy the object
     */
    boolean start() {
        if (clone != null || is(FAILED) || is(BUSY)) {
            return false;
        }
        setBusy(true);
        return true;
    }

    /**
     * Registers the clone. A single-phase copier may register it before it returns.
     *
     * @param clone clone
     * @return subscriptions to notify, or {@code null} if the clone is already registered
     */
    List<Subscription> register(Object clone) {
        if (this.clone != null) {
            return null;
        }
        this.clone = clone;
        signalChange();
        return takeSubscriptions();
    }

    /**
     * Registers the clone created by the first phase and the copier of the second phase which completes it.
     *
     * @param clone clone
     * @param copier copier which runs the second phase
     * @return subscriptions to notify
     */
    List<Subscription> created(Object clone, TwoPhaseObjectCopier<Object> copier) {
        this.clone = clone;
        this.copier = copier;
        this.state |= CLAIMABLE;
        setBusy(false);
        signalChange();
        return takeSubscriptions();
    }

    private List<Subscription> takeSubscriptions() {
        if (rare == null || rare.subscriptions == null) {
            return Collections.emptyList();
        }
        List<Subscription> taken = rare.subscriptions;
        rare.subscriptions = null;
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
        if (clone != null) {
            return clone;
        }
        if (is(FAILED)) {
            throw new ClonerException("Copying of a referenced object failed.");
        }
        Rare rare = rare();
        if (rare.subscriptions == null) {
            rare.subscriptions = new ArrayList<>(1);
        }
        rare.subscriptions.add(subscription);
        return null;
    }

    /**
     * Takes the second phase to run it in the current thread.
     *
     * @return {@code true} if the second phase is taken by the current thread
     */
    boolean claimSecondPhase() {
        if (!is(CLAIMABLE)) {
            return false;
        }
        state &= ~CLAIMABLE;
        setBusy(true);
        return true;
    }

    /**
     * Marks the clone of a single-phase copier completed.
     */
    void completed() {
        state |= COMPLETED;
        setBusy(false);
        signalChange();
    }

    /**
     * Marks the second phase done; the clone is completed when all holes are filled.
     */
    void secondPhaseDone() {
        state |= SECOND_PHASE_DONE;
        setBusy(false);
        completeIfNoHoles();
    }

    /**
     * Registers a hole: this slot waits for the clone of the blocker.
     *
     * @param blocker slot whose clone is waited for
     */
    void addHole(CloneSlot blocker) {
        Rare rare = rare();
        CloneSlot[] current = rare.blockers;
        CloneSlot[] updated = Arrays.copyOf(current, current.length + 1);
        updated[current.length] = blocker;
        rare.blockers = updated;
    }

    /**
     * Removes a hole after the clone of the blocker is set.
     *
     * @param blocker slot whose clone has been set
     */
    void holeFilled(CloneSlot blocker) {
        List<CloneSlot> updated = new ArrayList<>(Arrays.asList(rare.blockers));
        updated.remove(blocker);
        rare.blockers = updated.toArray(NO_SLOTS);
        completeIfNoHoles();
    }

    private void completeIfNoHoles() {
        if (is(SECOND_PHASE_DONE) && getBlockers().length == 0) {
            state |= COMPLETED;
        }
        signalChange();
    }

    /**
     * Marks the slot failed, e.g. because an object it waits for has failed.
     */
    void fail() {
        state |= FAILED;
        signalChange();
    }

    /**
     * Releases the slot after the current thread has finished its work, marks it failed if the work is not done.
     *
     * @param done whether the work is done
     * @return subscriptions which will never get the clone, if the slot has failed
     */
    List<Subscription> release(boolean done) {
        if (!isBusyByCurrentThread()) {
            return Collections.emptyList();
        }
        setBusy(false);
        if (!done) {
            state |= FAILED;
        }
        signalChange();
        return done ? Collections.emptyList() : takeSubscriptions();
    }

}
