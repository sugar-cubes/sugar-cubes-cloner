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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.CopyContext;
import io.github.sugarcubes.cloner.ObjectCopier;
import io.github.sugarcubes.cloner.TwoPhaseObjectCopier;
import io.github.sugarcubes.cloner.platform.JdkPlatform;

/**
 * Abstract copy context. Contains common code for the context implementations.
 *
 * <p>Every original object gets a {@link CloneSlot} which tracks whether its clone is created and completed and
 * which thread works on it. A second phase which meets an object being created right now subscribes to its clone
 * instead of waiting. A thread which has to wait for something which in turn waits for this thread (through busy
 * threads, holes and waiting threads) has found a reference cycle which cannot be copied.</p>
 *
 * @author Maxim Butov
 */
public abstract class AbstractCopyContext implements CopyContext {

    /**
     * Maximal time of a single wait, after which the waiting thread checks for a reference cycle again.
     */
    private static final long CYCLE_CHECK_INTERVAL_MILLIS = 100;

    /**
     * Copier provider.
     */
    private final CopierProvider copierProvider;

    /**
     * Copying states of the original objects.
     */
    private final SlotMap clones;

    /**
     * Creates slots for {@link SlotMap#getOrCreate}; kept in a field, a lambda capturing {@code this} would be
     * allocated for every copied object.
     */
    private final Function<Object, CloneSlot> slotFactory = original -> newSlot();

    /**
     * Slots which threads are waiting for, created on the first wait.
     */
    private Map<Thread, CloneSlot> waits;

    /**
     * Tasks invoked when the whole graph is copied, created on the first task.
     */
    private List<Callable<?>> afterCopy;

    /**
     * Platform of the running JDK.
     */
    private static final JdkPlatform JDK_PLATFORM = JdkPlatform.current();

    /**
     * Creates context with specified copier provider, clones map and predefined cloned objects.
     *
     * @param copierProvider copier provider
     * @param slots empty slot map
     * @param clones predefined cloned objects
     */
    AbstractCopyContext(CopierProvider copierProvider, SlotMap slots, Map<Object, Object> clones) {
        this.copierProvider = copierProvider;
        this.clones = slots;
        // system-wide singletons are their own clones
        JDK_PLATFORM.getSystemWideSingletons().forEach(singleton -> slots.put(singleton, new SingleThreadCloneSlot(singleton)));
        clones.forEach((key, value) -> slots.put(key, new SingleThreadCloneSlot(value)));
    }

    /**
     * Creates a slot for an object which is not copied yet.
     *
     * @return new slot
     */
    abstract CloneSlot newSlot();

    /**
     * Returns the slot whose second phase the current thread runs.
     *
     * @return slot or {@code null} outside a second phase
     */
    abstract CloneSlot getOwner();

    /**
     * Sets the slot whose second phase the current thread runs.
     *
     * @param owner slot or {@code null}
     */
    abstract void setOwner(CloneSlot owner);

    @Override
    public <T> T register(T clone) {
        throw new ClonerException("Registration must be called from copy() method.");
    }

    @Override
    public <T> T copy(T original) throws Exception {
        if (original == null) {
            return null;
        }

        ObjectCopier<T> copier = copierProvider.getCopier(original);

        // trivial cases
        if (copier == ObjectCopier.NOOP) {
            return original;
        }
        if (copier == ObjectCopier.NULL) {
            return null;
        }

        // non-trivial case
        return doClone(original, copier);
    }

    @Override
    public Object copyNow(Object original) throws Exception {
        if (original == null || getOwner() == null) {
            return copy(original);
        }
        ObjectCopier<Object> copier = copierProvider.getCopier(original);
        if (copier == ObjectCopier.NOOP) {
            return original;
        }
        if (copier == ObjectCopier.NULL) {
            return null;
        }
        CloneSlot slot = clones.getOrCreate(original, slotFactory);
        Object existing = slot.getClone();
        if (existing != null) {
            return existing;
        }
        return slot.start() ? create(original, copier, slot) : PENDING;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void subscribe(T original, CloneSetter<? super T> setter) throws Exception {
        if (original == null || getOwner() == null) {
            setter.set(copy(original));
            return;
        }
        Object clone = copyNow(original);
        if (clone != PENDING) {
            setter.set((T) clone);
            return;
        }
        CloneSlot owner = getOwner();
        CloneSlot slot = clones.get(original);
        owner.addHole(slot);
        clone = slot.subscribe(new CloneSlot.Subscription(owner, (CloneSetter<Object>) setter));
        if (clone != null) {
            setter.set((T) clone);
            owner.holeFilled(slot);
        }
    }

    @Override
    public <T> T copyComplete(T original) throws Exception {
        T clone = copy(original);
        if (clone == null || clone == original) {
            return clone;
        }
        CloneSlot slot = clones.get(original);
        while (!slot.isCompleted()) {
            if (!runSecondPhase(slot, original) && !await(slot, original, CloneSlot::isCompleted)) {
                // waits for an object being created by the current thread
                return clone;
            }
        }
        return clone;
    }

    @Override
    public boolean isComplete(Object original) {
        CloneSlot slot = original != null ? clones.get(original) : null;
        return slot == null || slot.isCompleted();
    }

    /**
     * Complex copying which must return non-null and non-original object.
     *
     * @param <T> object type
     * @param original original object
     * @param copier object copier
     * @return copy of the original object
     * @throws Exception if something went wrong
     */
    @SuppressWarnings("unchecked")
    protected <T> T doClone(T original, ObjectCopier<T> copier) throws Exception {
        CloneSlot slot = clones.getOrCreate(original, slotFactory);
        Object existing = slot.getClone();
        if (existing != null) {
            return (T) existing;
        }
        if (slot.start()) {
            return create(original, copier, slot);
        }
        if (!await(slot, original, CloneSlot::isCreated)) {
            throw cycle(original);
        }
        return (T) slot.getClone();
    }

    /**
     * Creates the clone in the current thread, which has started the slot.
     */
    private <T> T create(T original, ObjectCopier<T> copier, CloneSlot slot) throws Exception {
        try {
            return copier instanceof TwoPhaseObjectCopier
                ? createTwoPhase(original, (TwoPhaseObjectCopier<T>) copier, slot)
                : createSinglePhase(original, copier, slot);
        }
        finally {
            failSubscriptions(slot.release(false));
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createTwoPhase(T original, TwoPhaseObjectCopier<T> copier, CloneSlot slot) throws Exception {
        T clone = checkClone(original, copier.allocate(original));
        notifySubscriptions(slot, clone, slot.created(clone, (TwoPhaseObjectCopier<Object>) copier));
        thenInvoke(() -> runSecondPhase(slot, original));
        return clone;
    }

    private <T> T createSinglePhase(T original, ObjectCopier<T> copier, CloneSlot slot) throws Exception {
        // a single-phase copier gets clones at once, it has no second phase to complete later
        CloneSlot owner = getOwner();
        setOwner(null);
        try {
            RegistrationContext registration = new RegistrationContext(this, original, slot);
            T clone = checkClone(original, copier.copy(original, registration));
            registration.check(clone);
            slot.completed();
            return clone;
        }
        finally {
            setOwner(owner);
        }
    }

    private static <T> T checkClone(T original, T clone) {
        if (clone == null) {
            throw new ClonerException(String.format("Non-null clone expected for '%s'.", original));
        }
        return clone;
    }

    /**
     * Registers the clone created by a single-phase copier, see {@link RegistrationContext}.
     *
     * @param slot slot
     * @param clone clone
     * @return {@code false} if the clone is already registered
     * @throws Exception if a subscriber fails
     */
    boolean register(CloneSlot slot, Object clone) throws Exception {
        List<CloneSlot.Subscription> subscriptions = slot.register(clone);
        if (subscriptions == null) {
            return false;
        }
        notifySubscriptions(slot, clone, subscriptions);
        return true;
    }

    /**
     * Passes the clone to the subscribers; subscribers which have not got it because of an exception fail.
     */
    private static void notifySubscriptions(CloneSlot slot, Object clone, List<CloneSlot.Subscription> subscriptions)
        throws Exception {
        if (subscriptions.isEmpty()) {
            return;
        }
        int notified = 0;
        try {
            for (CloneSlot.Subscription subscription : subscriptions) {
                subscription.setter.set(clone);
                subscription.owner.holeFilled(slot);
                notified++;
            }
        }
        finally {
            failSubscriptions(subscriptions.subList(notified, subscriptions.size()));
        }
    }

    private static void failSubscriptions(List<CloneSlot.Subscription> subscriptions) {
        subscriptions.forEach(subscription -> subscription.owner.fail());
    }

    /**
     * Runs the second phase of the copying in the current thread unless another thread has taken it.
     *
     * @param slot slot
     * @param original original object of the slot
     * @return {@code true} if the second phase has been run by the current thread
     * @throws Exception if something went wrong
     */
    private boolean runSecondPhase(CloneSlot slot, Object original) throws Exception {
        if (!slot.claimSecondPhase()) {
            return false;
        }
        CloneSlot owner = getOwner();
        setOwner(slot);
        try {
            slot.getCopier().deepCopy(original, slot.getClone(), this);
            slot.secondPhaseDone();
        }
        finally {
            setOwner(owner);
            slot.release(false);
        }
        return true;
    }

    /**
     * Waits until the slot is ready or its second phase can be claimed.
     *
     * @return {@code false} if waiting would never end because of a reference cycle
     */
    private boolean await(CloneSlot slot, Object original, Predicate<CloneSlot> ready) throws InterruptedException {
        Thread current = Thread.currentThread();
        Map<Thread, CloneSlot> waits = waits();
        waits.put(current, slot);
        try {
            while (!ready.test(slot)) {
                if (slot.isFailed()) {
                    throw new ClonerException(String.format("Copying of %s failed.", describe(original)));
                }
                if (slot.isClaimable()) {
                    return true;
                }
                if (dependsOn(slot, current, waits)) {
                    return false;
                }
                slot.awaitChange(ready, CYCLE_CHECK_INTERVAL_MILLIS);
            }
            return true;
        }
        finally {
            waits.remove(current);
        }
    }

    private synchronized Map<Thread, CloneSlot> waits() {
        if (waits == null) {
            waits = new ConcurrentHashMap<>();
        }
        return waits;
    }

    /**
     * Tells whether the slot waits, directly or transitively, for the given thread: through the thread busy with a
     * slot, the holes of a slot and the slot a thread waits for.
     */
    private static boolean dependsOn(CloneSlot start, Thread thread, Map<Thread, CloneSlot> waits) {
        Set<CloneSlot> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<CloneSlot> pending = new ArrayDeque<>();
        pending.push(start);
        while (!pending.isEmpty()) {
            CloneSlot slot = pending.pop();
            if (!visited.add(slot)) {
                continue;
            }
            Thread busy = slot.getBusyThread();
            if (busy == thread) {
                return true;
            }
            if (busy != null) {
                CloneSlot awaited = waits.get(busy);
                if (awaited != null) {
                    pending.push(awaited);
                }
            }
            for (CloneSlot blocker : slot.getBlockers()) {
                pending.push(blocker);
            }
        }
        return false;
    }

    private static ClonerException cycle(Object original) {
        return new ClonerException(String.format("Cannot clone %s: it is part of a reference cycle with an object "
            + "which needs it ready before its own clone exists. Copiers creating the clone from copied values "
            + "(records, Copyable, custom single-phase copiers) do not support such cycles.", describe(original)));
    }

    private static String describe(Object original) {
        return String.format("%s@%x", original.getClass().getName(), System.identityHashCode(original));
    }

    @Override
    public synchronized void afterCopy(Callable<?> task) {
        if (afterCopy == null) {
            afterCopy = new ArrayList<>();
        }
        afterCopy.add(task);
    }

    /**
     * Invokes the tasks registered with {@link #afterCopy(Callable)}. Must be called after {@link #complete()}.
     *
     * @throws Exception if something went wrong
     */
    public void runAfterCopy() throws Exception {
        List<Callable<?>> tasks;
        synchronized (this) {
            tasks = afterCopy;
            afterCopy = null;
        }
        if (tasks != null) {
            for (Callable<?> task : tasks) {
                task.call();
            }
        }
    }

    /**
     * Completes all the delayed tasks.
     *
     * @throws Throwable if something went wrong
     */
    public abstract void complete() throws Throwable;

}
