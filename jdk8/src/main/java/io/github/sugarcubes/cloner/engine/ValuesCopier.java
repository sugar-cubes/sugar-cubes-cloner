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

import java.lang.reflect.Field;
import java.util.function.Supplier;

import io.github.sugarcubes.cloner.ClonerException;
import io.github.sugarcubes.cloner.CopyContext;
import io.github.sugarcubes.cloner.CopyPolicy;
import io.github.sugarcubes.cloner.ObjectCopier;
import io.github.sugarcubes.cloner.spi.ValuesModel;
import io.github.sugarcubes.cloner.util.Checks;
import io.github.sugarcubes.cloner.util.PrimitiveType;

/**
 * Copier of objects created from the values of their components, e.g. records. Gets from the low-level
 * {@link ValuesModel} how to read the values and to create an instance, and decides itself what to copy: copies the
 * values completely before creating the clone, so the constructor may validate or copy them.
 *
 * <p>An object may be part of a reference cycle, e.g. a record may contain itself in a list component. Then the list
 * cannot be completed before the clone exists, and the constructor gets it incomplete. This works if the constructor
 * keeps the component as is: the list is completed after the clone is registered. If the constructor replaces or
 * reads it (defensive copy, validation), the object cannot be cloned and {@link ClonerException} is thrown.</p>
 *
 * @param <T> type
 * @author Maxim Butov
 */
public class ValuesCopier<T> implements ObjectCopier<T> {

    private final ValuesModel<T> model;

    private final CopyPolicy<Field> policy;

    private final FieldValues values;

    private final boolean shallow;

    /**
     * Rules of the components, created on the first copy: the model does not tell the number of the components.
     */
    private volatile Rule[] rules;

    /**
     * What the clone gets in a component.
     */
    private static final class Rule {

        static final Rule COPY = new Rule(null, null);

        static final Rule ORIGINAL = new Rule(null, null);

        /**
         * Field of the component and the supplier of its value, or {@code null} for {@link #COPY} and
         * {@link #ORIGINAL}.
         */
        final Field field;

        final Supplier<?> value;

        Rule(Field field, Supplier<?> value) {
            this.field = field;
            this.value = value;
        }

    }

    /**
     * Creates the copier.
     *
     * @param model low-level model of the type
     * @param shallow shallow mode: values are passed to the constructor as is
     */
    public ValuesCopier(ValuesModel<T> model, boolean shallow) {
        this(model, CopyPolicy.defaultPolicy(), FieldValues.NONE, shallow);
    }

    /**
     * Creates the copier which applies the field actions and values to the components which have fields, see
     * {@link ValuesModel#fieldOf(int)}.
     *
     * @param model low-level model of the type
     * @param policy field copy policy
     * @param values values of fields, which have priority over the policy
     * @param shallow shallow mode: values are passed to the constructor as is
     */
    public ValuesCopier(ValuesModel<T> model, CopyPolicy<Field> policy, FieldValues values, boolean shallow) {
        this.model = model;
        this.policy = policy;
        this.values = values;
        this.shallow = shallow;
    }

    private Rule[] rules(int count) {
        Rule[] known = rules;
        if (known == null) {
            known = new Rule[count];
            for (int k = 0; k < count; k++) {
                known[k] = rule(model.fieldOf(k));
            }
            rules = known;
        }
        return known;
    }

    private Rule rule(Field field) {
        Rule byDefault = shallow ? Rule.ORIGINAL : Rule.COPY;
        if (field == null) {
            return byDefault;
        }
        Supplier<?> value = values.valueOf(field);
        if (value != null) {
            return new Rule(field, value);
        }
        switch (policy.getAction(field)) {
            case DEFAULT:
                return byDefault;
            case ORIGINAL:
                return Rule.ORIGINAL;
            case CLEAR:
                Object zero = PrimitiveType.defaultValue(field.getType());
                return new Rule(field, () -> zero);
            default:
                throw Checks.mustNotHappen();
        }
    }

    @Override
    public T copy(T original, CopyContext context) throws Exception {
        Object[] originals = model.read(original);
        Rule[] rules = rules(originals.length);
        Object[] copies = new Object[originals.length];
        boolean[] incomplete = new boolean[originals.length];
        boolean anyIncomplete = false;
        for (int k = 0; k < originals.length; k++) {
            Rule rule = rules[k];
            if (rule == Rule.ORIGINAL) {
                copies[k] = originals[k];
            }
            else if (rule != Rule.COPY) {
                copies[k] = FieldValues.check(rule.field, rule.value.get());
            }
            else {
                copies[k] = context.copyComplete(originals[k]);
            }
        }
        // checked after all copies: copying of a later component may complete an earlier one
        for (int k = 0; k < originals.length; k++) {
            incomplete[k] = rules[k] == Rule.COPY && !context.isComplete(originals[k]);
            anyIncomplete |= incomplete[k];
        }
        if (!anyIncomplete) {
            return context.register(model.create(copies));
        }
        return context.register(createWithIncomplete(original, copies, incomplete));
    }

    /**
     * Creates the clone from values some of which are not complete yet because of a reference cycle, and checks that
     * the constructor keeps them as is.
     */
    private T createWithIncomplete(T original, Object[] copies, boolean[] incomplete) {
        T clone;
        try {
            clone = model.create(copies);
        }
        catch (ClonerException e) {
            throw cycleError(original, "its constructor fails", e.getCause());
        }
        Object[] kept = model.read(clone);
        for (int k = 0; k < copies.length; k++) {
            if (incomplete[k] && kept[k] != copies[k]) {
                throw cycleError(original, String.format("its constructor replaces the component '%s'", model.nameOf(k)),
                    null);
            }
        }
        return clone;
    }

    private static ClonerException cycleError(Object original, String reason, Throwable cause) {
        return new ClonerException(String.format("Cannot clone %s: it is part of a reference cycle, so some of its "
            + "components are not completely copied when the clone is created, and %s.",
            original.getClass().getName(), reason), cause);
    }

}
