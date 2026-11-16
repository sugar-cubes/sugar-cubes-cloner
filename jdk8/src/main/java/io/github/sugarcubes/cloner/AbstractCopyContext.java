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

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
    private final Map<IdentityReference<Object>, CloneSlot> clones;

    /**
     * Slots which threads are waiting for.
     */
    private final Map<Thread, CloneSlot> waits = new ConcurrentHashMap<>();

    /**
     * JDK configuration.
     */
    private static final JdkConfiguration JDK_CONFIGURATION = JdkConfigurationHolder.CONFIGURATION;

    /**
     * Predefined system-wide singletons as map.
     */
    @SuppressWarnings("checkstyle:Indentation")
    private static final Map<IdentityReference<Object>, CloneSlot> SYSTEM_SINGLETONS_AS_MAP =
        JDK_CONFIGURATION.getSystemWideSingletons().stream()
            .collect(Collectors.toMap(IdentityReference::new, CloneSlot::new));

    /**
     * Creates context with specified copier provider, clones map and predefined cloned objects.
     *
     * @param copierProvider copier provider
     * @param cacheSupplier cache supplier
     * @param clones predefined cloned objects
     */
    protected AbstractCopyContext(CopierProvider copierProvider, Supplier<Map<?, ?>> cacheSupplier, Map<Object, Object> clones) {
        this.copierProvider = copierProvider;
        this.clones = (Map) cacheSupplier.get();
        this.clones.putAll(SYSTEM_SINGLETONS_AS_MAP);
        clones.forEach((key, value) -> this.clones.put(new IdentityReference<>(key), new CloneSlot(value)));
    }

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
    public <T> void copy(T original, CloneSetter<? super T> setter) throws Exception {
        setter.set(copy(original));
    }

    /**
     * Copies the object for the second phase of the owner: if the object is being created right now, the setter is
     * called later and the owner gets a hole until then.
     */
    @SuppressWarnings("unchecked")
    private <T> void copy(T original, CloneSetter<? super T> setter, CloneSlot owner) throws Exception {
        if (original == null) {
            setter.set(null);
            return;
        }
        ObjectCopier<T> copier = copierProvider.getCopier(original);
        if (copier == ObjectCopier.NOOP) {
            setter.set(original);
            return;
        }
        if (copier == ObjectCopier.NULL) {
            setter.set(null);
            return;
        }
        CloneSlot slot = clones.computeIfAbsent(new IdentityReference<>(original), key -> new CloneSlot());
        Object existing = slot.getClone();
        if (existing != null) {
            setter.set((T) existing);
            return;
        }
        if (slot.start()) {
            setter.set(create(original, copier, slot));
            return;
        }
        owner.addHole(slot);
        Object clone = slot.subscribe(new CloneSlot.Subscription(owner, (CloneSetter<Object>) setter));
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
        CloneSlot slot = clones.get(new IdentityReference<>(original));
        while (!slot.isCompleted()) {
            if (!runSecondPhase(slot) && !await(slot, original, CloneSlot::isCompleted)) {
                // waits for an object being created by the current thread
                return clone;
            }
        }
        return clone;
    }

    @Override
    public boolean isComplete(Object original) {
        CloneSlot slot = original != null ? clones.get(new IdentityReference<>(original)) : null;
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
        CloneSlot slot = clones.computeIfAbsent(new IdentityReference<>(original), key -> new CloneSlot());
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

    private <T> T createTwoPhase(T original, TwoPhaseObjectCopier<T> copier, CloneSlot slot) throws Exception {
        T clone = checkClone(original, copier.allocate(original));
        CopyContext secondPhaseContext = new SecondPhaseContext(slot);
        notifySubscriptions(slot, clone, slot.created(clone, () -> {
            copier.deepCopy(original, clone, secondPhaseContext);
            return null;
        }));
        thenInvoke(() -> runSecondPhase(slot));
        return clone;
    }

    private <T> T createSinglePhase(T original, ObjectCopier<T> copier, CloneSlot slot) throws Exception {
        RegistrationContext registration = new RegistrationContext(this, original, slot);
        T clone = checkClone(original, copier.copy(original, registration));
        registration.check(clone);
        slot.completed();
        return clone;
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
     * @return {@code true} if the second phase has been run by the current thread
     * @throws Exception if something went wrong
     */
    private boolean runSecondPhase(CloneSlot slot) throws Exception {
        Callable<?> secondPhase = slot.claimSecondPhase();
        if (secondPhase == null) {
            return false;
        }
        try {
            secondPhase.call();
            slot.secondPhaseDone();
        }
        finally {
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
        waits.put(current, slot);
        try {
            while (!ready.test(slot)) {
                if (slot.isFailed()) {
                    throw new ClonerException(String.format("Copying of %s failed.", describe(original)));
                }
                if (slot.isClaimable()) {
                    return true;
                }
                if (dependsOn(slot, current)) {
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

    /**
     * Tells whether the slot waits, directly or transitively, for the given thread: through the thread busy with a
     * slot, the holes of a slot and the slot a thread waits for.
     */
    private boolean dependsOn(CloneSlot start, Thread thread) {
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

    /**
     * Context of the second phase of a two-phase copier: referenced objects being created right now become holes
     * of the slot instead of being waited for.
     */
    private final class SecondPhaseContext implements CopyContext {

        private final CloneSlot slot;

        SecondPhaseContext(CloneSlot slot) {
            this.slot = slot;
        }

        @Override
        public <T> T register(T clone) {
            return AbstractCopyContext.this.register(clone);
        }

        @Override
        public <T> T copy(T original) throws Exception {
            return AbstractCopyContext.this.copy(original);
        }

        @Override
        public <T> void copy(T original, CloneSetter<? super T> setter) throws Exception {
            AbstractCopyContext.this.copy(original, setter, slot);
        }

        @Override
        public <T> T copyComplete(T original) throws Exception {
            return AbstractCopyContext.this.copyComplete(original);
        }

        @Override
        public boolean isComplete(Object original) {
            return AbstractCopyContext.this.isComplete(original);
        }

        @Override
        public void thenInvoke(Callable<?> task) throws Exception {
            AbstractCopyContext.this.thenInvoke(task);
        }

    }

    /**
     * Completes all the delayed tasks.
     *
     * @throws Throwable if something went wrong
     */
    public abstract void complete() throws Throwable;

}
