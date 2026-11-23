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
package io.github.sugarcubes.cloner.engine;

import java.util.List;
import java.util.function.Predicate;

import io.github.sugarcubes.cloner.TwoPhaseObjectCopier;

/**
 * Slot for the parallel context: the state machine is synchronized on the slot, waiting threads use
 * {@link Object#wait(long)}.
 *
 * @author Maxim Butov
 */
final class ConcurrentCloneSlot extends CloneSlot {

    /**
     * Thread which creates or completes the clone, {@code null} if nobody does.
     */
    private Thread busyThread;

    /**
     * Creates a slot for an object which is not copied yet.
     *
     * @param original original object
     */
    ConcurrentCloneSlot(Object original) {
        super(original);
    }

    @Override
    protected void signalChange() {
        // called under the monitor by the synchronized methods below
        notifyAll();
    }

    @Override
    synchronized void awaitChange(Predicate<CloneSlot> ready, long timeoutMillis) throws InterruptedException {
        if (mustWait(ready)) {
            wait(timeoutMillis);
        }
    }

    @Override
    synchronized Object getClone() {
        return super.getClone();
    }

    @Override
    synchronized boolean isCompleted() {
        return super.isCompleted();
    }

    @Override
    synchronized boolean isFailed() {
        return super.isFailed();
    }

    @Override
    synchronized boolean isClaimable() {
        return super.isClaimable();
    }

    @Override
    synchronized Thread getBusyThread() {
        return busyThread;
    }

    @Override
    synchronized boolean isBusyByCurrentThread() {
        return busyThread == Thread.currentThread();
    }

    @Override
    synchronized void setBusy(boolean busy) {
        super.setBusy(busy);
        busyThread = busy ? Thread.currentThread() : null;
    }

    @Override
    synchronized CloneSlot[] getBlockers() {
        return super.getBlockers();
    }

    @Override
    synchronized TwoPhaseObjectCopier<Object> getCopier() {
        return super.getCopier();
    }

    @Override
    synchronized boolean start() {
        return super.start();
    }

    @Override
    synchronized List<Subscription> register(Object clone) {
        return super.register(clone);
    }

    @Override
    synchronized List<Subscription> created(Object clone, TwoPhaseObjectCopier<Object> copier) {
        return super.created(clone, copier);
    }

    @Override
    synchronized Object subscribe(Subscription subscription) {
        return super.subscribe(subscription);
    }

    @Override
    synchronized boolean claimSecondPhase() {
        return super.claimSecondPhase();
    }

    @Override
    synchronized void completed() {
        super.completed();
    }

    @Override
    synchronized void secondPhaseDone() {
        super.secondPhaseDone();
    }

    @Override
    synchronized void addHole(CloneSlot blocker) {
        super.addHole(blocker);
    }

    @Override
    synchronized void holeFilled(CloneSlot blocker) {
        super.holeFilled(blocker);
    }

    @Override
    synchronized void fail() {
        super.fail();
    }

    @Override
    synchronized List<Subscription> release(boolean done) {
        return super.release(done);
    }

}
