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
import java.util.List;
import java.util.concurrent.Callable;

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.CopyContext;
import io.github.sugarcubes.cloner.spi.FieldAdapter;
import io.github.sugarcubes.cloner.util.ClonerExceptionUtils;

/**
 * A copy context which registers clone in the cache.
 *
 * @author Maxim Butov
 */
public final class RegistrationContext implements CopyContext {

    /**
     * Initial copy context.
     */
    private final AbstractCopyContext context;

    /**
     * Original object.
     */
    private final Object original;

    /**
     * Copying state of the original.
     */
    private final CloneSlot slot;

    /**
     * Holes of a copy constructor, created on the first hole.
     */
    private List<Hole> holes;

    /**
     * Field of a copy constructor waiting for the clone of its value.
     */
    private static final class Hole {

        final Object value;

        final CloneSetter<Object> setter;

        Hole(Object value, CloneSetter<Object> setter) {
            this.value = value;
            this.setter = setter;
        }

    }

    /**
     * Creates registration context wrapper.
     *
     * @param context initial context
     * @param original original
     * @param slot copying state of the original
     */
    RegistrationContext(AbstractCopyContext context, Object original, CloneSlot slot) {
        this.context = context;
        this.original = original;
        this.slot = slot;
    }

    /**
     * Throws exception with message describing registration error.
     */
    private void registrationMismatch() {
        throw new ClonerException(String.format("Registration mismatch when cloning '%s'. " +
            "If you use custom ObjectCopier or Copyable objects, " +
            "ensure that you call CopyContext.register(clone) method " +
            "and call it exactly once.", original));
    }

    @Override
    public <T> T register(T clone) {
        if (clone == null || !ClonerExceptionUtils.replaceException(() -> context.register(slot, clone))) {
            registrationMismatch();
        }
        return clone;
    }

    @Override
    public <T> T copy(T original) throws Exception {
        return context.copy(original);
    }

    /**
     * Remembers a hole of the copy constructor which has got {@link CopyContext#PENDING} for a field value: the field
     * is written when the clone of the value is ready, after the constructor has returned (the constructor writes
     * {@code null} to it).
     *
     * @param value original value of the field
     * @param target clone holding the field
     * @param declaringClass class declaring the field
     * @param field name of the field
     */
    public void subscribeField(Object value, Object target, Class<?> declaringClass, String field) {
        FieldAdapter adapter = context.fieldAdapter(declaringClass, field);
        if (holes == null) {
            holes = new ArrayList<>();
        }
        holes.add(new Hole(value, clone -> adapter.setObject(target, clone)));
    }

    /**
     * Subscribes the holes remembered by the copy constructor, after it has returned.
     *
     * @throws Exception if a field cannot be written
     */
    void subscribeHoles() throws Exception {
        if (holes == null) {
            return;
        }
        for (Hole hole : holes) {
            Object clone = context.subscribePending(hole.value, hole.setter);
            if (clone != null) {
                hole.setter.set(clone);
            }
        }
    }

    @Override
    public Object copyNow(Object original) throws Exception {
        return context.copyNow(original);
    }

    @Override
    public <T> void subscribe(T original, CloneSetter<? super T> setter) throws Exception {
        context.subscribe(original, setter);
    }

    @Override
    public <T> T copyComplete(T original) throws Exception {
        return context.copyComplete(original);
    }

    @Override
    public boolean isComplete(Object original) {
        return context.isComplete(original);
    }

    @Override
    public void thenInvoke(Callable<?> task) throws Exception {
        context.thenInvoke(task);
    }

    @Override
    public void afterCopy(Callable<?> task) {
        context.afterCopy(task);
    }

    /**
     * Checks that the cached clone is the same as returned from {@link #copy(Object)} method.
     *
     * @param clone clone
     */
    public void check(Object clone) {
        if (slot.getClone() != clone) {
            registrationMismatch();
        }
    }

}
