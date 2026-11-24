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

import java.util.function.Supplier;


import io.github.sugarcubes.cloner.CopyContext;
import io.github.sugarcubes.cloner.TwoPhaseObjectCopier;
import io.github.sugarcubes.cloner.spi.ContainerModel;

/**
 * Copier of collections and maps which hash their keys. A hash code of a copied key may be final only when the whole
 * object graph is copied, so the copier chooses for every container:
 * <ul>
 *     <li>if the hash codes of all keys are known at once (keys which are not copied, e.g. strings and enums, or keys
 *     hashed by identity), the container is rebuilt from copied elements by {@link ContainerCopier} and is complete
 *     after its second phase;</li>
 *     <li>otherwise it is copied by fields, keeping the hash codes of the original keys, which are right for keys
 *     hashed by their state. After the whole graph is copied, the container is checked and rebuilt from its own
 *     elements if some key is not found. Until then its content may be incomplete for the copiers which need it
 *     complete, e.g. a record constructor making a defensive copy.</li>
 * </ul>
 *
 * @param <T> container type
 * @author Maxim Butov
 */
public class HashContainerCopier<T> extends TwoPhaseObjectCopier<T> {

    private final ContainerCopier<T> rebuilding;

    private final Supplier<TwoPhaseObjectCopier<T>> fields;

    private final ContainerModel<T> model;

    private final boolean shallow;

    /**
     * Creates the copier.
     *
     * @param fields copier of the container fields, needed only for keys hashed by their state
     * @param model low-level model of the container
     * @param shallow shallow mode: keys are the original ones
     */
    public HashContainerCopier(Supplier<TwoPhaseObjectCopier<T>> fields, ContainerModel<T> model, boolean shallow) {
        this.rebuilding = new ContainerCopier<>(model, shallow);
        this.fields = fields;
        this.model = model;
        this.shallow = shallow;
    }

    @Override
    public T allocate(T original) throws Exception {
        return hashCodesKnownAtOnce(original) ? rebuilding.allocate(original) : fields.get().allocate(original);
    }

    @Override
    public void deepCopy(T original, T clone, CopyContext context) throws Exception {
        if (hashCodesKnownAtOnce(original)) {
            rebuilding.deepCopy(original, clone, context);
            return;
        }
        fields.get().deepCopy(original, clone, context);
        context.afterCopy(() -> {
            rebuildIfKeysNotFound(clone);
            return null;
        });
    }

    private boolean hashCodesKnownAtOnce(T original) {
        if (shallow) {
            return true;
        }
        return HashKeys.knownAtOnce(model.elements(original), model.isMap());
    }

    private void rebuildIfKeysNotFound(T clone) {
        Object[] elements = model.elements(clone);
        int step = model.isMap() ? 2 : 1;
        for (int k = 0; k < elements.length; k += step) {
            if (!model.containsKey(clone, elements[k])) {
                model.clear(clone);
                model.fill(clone, elements);
                return;
            }
        }
    }

}
