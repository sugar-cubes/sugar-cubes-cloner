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

import java.util.concurrent.atomic.AtomicInteger;

import io.github.sugarcubes.cloner.CopyContext;
import io.github.sugarcubes.cloner.TwoPhaseObjectCopier;
import io.github.sugarcubes.cloner.spi.ContainerModel;

/**
 * Copier of collections and maps which do not hash their keys. Gets from the low-level {@link ContainerModel} how to
 * create an empty container, to list and to put elements, and decides itself what to copy: rebuilds the container
 * from copied elements instead of copying its internal structure.
 *
 * <p>Elements being created right now (reference cycles) are put later; elements are put in order when all of them
 * are known.</p>
 *
 * @see HashContainerCopier
 *
 * @param <T> container type
 * @author Maxim Butov
 */
public class ContainerCopier<T> extends TwoPhaseObjectCopier<T> {

    private final ContainerModel<T> model;

    private final boolean shallow;

    /**
     * Creates the copier.
     *
     * @param model low-level model of the container
     * @param shallow shallow mode: elements are put as is
     */
    public ContainerCopier(ContainerModel<T> model, boolean shallow) {
        this.model = model;
        this.shallow = shallow;
    }

    @Override
    public T allocate(T original) throws Exception {
        return model.create(original);
    }

    @Override
    public void deepCopy(T original, T clone, CopyContext context) throws Exception {
        Object[] elements = model.elements(original);
        if (shallow) {
            model.fill(clone, elements);
            return;
        }
        int[] pending = null;
        int pendingCount = 0;
        for (int k = 0, length = elements.length; k < length; k++) {
            Object copy = context.copyNow(elements[k]);
            if (copy != CopyContext.PENDING) {
                elements[k] = copy;
            }
            else {
                if (pending == null) {
                    pending = new int[length];
                }
                pending[pendingCount++] = k;
            }
        }
        if (pendingCount == 0) {
            model.fill(clone, elements);
            return;
        }
        AtomicInteger unknown = new AtomicInteger(pendingCount + 1);
        for (int k = 0; k < pendingCount; k++) {
            int index = pending[k];
            Object element = elements[index];
            elements[index] = null;
            context.subscribe(element, later -> {
                elements[index] = later;
                fillIfKnown(clone, elements, unknown);
            });
        }
        fillIfKnown(clone, elements, unknown);
    }

    private void fillIfKnown(T clone, Object[] elements, AtomicInteger unknown) {
        if (unknown.decrementAndGet() == 0) {
            model.fill(clone, elements);
        }
    }

}
