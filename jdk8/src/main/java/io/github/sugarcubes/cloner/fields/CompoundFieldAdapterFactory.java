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
package io.github.sugarcubes.cloner.fields;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github.sugarcubes.cloner.spi.FieldAdapter;
import io.github.sugarcubes.cloner.spi.FieldAdapterFactory;
import io.github.sugarcubes.cloner.util.Checks;

/**
 * Field adapter factory which uses for every field the first factory able to access it, e.g. reflection, then Unsafe
 * for fields in packages not opened to the cloner.
 *
 * @author Maxim Butov
 */
public final class CompoundFieldAdapterFactory implements FieldAdapterFactory {

    private final List<FieldAdapterFactory> factories;

    /**
     * Constructor.
     *
     * @param factories field adapter factories in order of preference
     */
    public CompoundFieldAdapterFactory(List<FieldAdapterFactory> factories) {
        Checks.illegalArg(factories.isEmpty(), "Must be at least one field adapter factory.");
        this.factories = new ArrayList<>(factories);
    }

    @Override
    public Optional<FieldAdapter> newAdapter(Field field) {
        for (FieldAdapterFactory factory : factories) {
            Optional<FieldAdapter> adapter = factory.newAdapter(field);
            if (adapter.isPresent()) {
                return adapter;
            }
        }
        return Optional.empty();
    }

}
