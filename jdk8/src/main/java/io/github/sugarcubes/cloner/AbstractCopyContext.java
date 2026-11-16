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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Abstract copy context. Contains common code for the context implementations.
 *
 * <p>Every original object gets a {@link CloneSlot} which tracks whether its clone is created and completed and
 * which thread works on it. A thread which needs a clone being created or completed by itself has found a reference
 * cycle which cannot be copied; in parallel mode the same is detected through the chain of waiting threads.</p>
 *
 * @author Maxim Butov
 */
public abstract class AbstractCopyContext implements CopyContext {

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
    public <T> T copyComplete(T original) throws Exception {
        T clone = copy(original);
        if (clone == null || clone == original) {
            return clone;
        }
        CloneSlot slot = clones.get(new IdentityReference<>(original));
        while (!slot.isCompleted()) {
            if (!runSecondPhase(slot)) {
                await(slot, original, CloneSlot::isCompleted);
            }
        }
        return clone;
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
        if (!slot.start()) {
            await(slot, original, CloneSlot::isCreated);
            return (T) slot.getClone();
        }
        try {
            return copier instanceof TwoPhaseObjectCopier
                ? createTwoPhase(original, (TwoPhaseObjectCopier<T>) copier, slot)
                : createSinglePhase(original, copier, slot);
        }
        finally {
            slot.release(false);
        }
    }

    private <T> T createTwoPhase(T original, TwoPhaseObjectCopier<T> copier, CloneSlot slot) throws Exception {
        T clone = checkClone(original, copier.allocate(original));
        slot.created(clone, () -> {
            copier.deepCopy(original, clone, this);
            return null;
        });
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
            slot.completed();
        }
        finally {
            slot.release(false);
        }
        return true;
    }

    /**
     * Waits until the slot is ready. Waiting for itself or a cycle of waiting threads means a reference cycle which
     * cannot be copied.
     */
    private void await(CloneSlot slot, Object original, Predicate<CloneSlot> ready) throws InterruptedException {
        Thread current = Thread.currentThread();
        slot.lock();
        try {
            while (!ready.test(slot)) {
                if (slot.isFailed()) {
                    throw new ClonerException(String.format("Copying of %s failed.", describe(original)));
                }
                Thread busy = slot.getBusyThread();
                if (busy == null) {
                    // the second phase is waiting to be claimed
                    return;
                }
                if (busy == current) {
                    throw cycle(original);
                }
                waits.put(current, slot);
                try {
                    if (isWaitingForItself(current, slot)) {
                        throw cycle(original);
                    }
                    slot.awaitChange();
                }
                finally {
                    waits.remove(current);
                }
            }
        }
        finally {
            slot.unlock();
        }
    }

    private boolean isWaitingForItself(Thread current, CloneSlot slot) {
        Thread thread = slot.getBusyThread();
        for (int k = 0; thread != null && k <= waits.size(); k++) {
            if (thread == current) {
                return true;
            }
            CloneSlot next = waits.get(thread);
            if (next == null) {
                return false;
            }
            thread = next.getBusyThread();
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
     * Completes all the delayed tasks.
     *
     * @throws Throwable if something went wrong
     */
    public abstract void complete() throws Throwable;

}
