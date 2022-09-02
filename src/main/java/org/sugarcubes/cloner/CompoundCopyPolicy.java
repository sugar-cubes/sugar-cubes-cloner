package org.sugarcubes.cloner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Compound copy policy. Searches for the first non-default action in the list of policies.
 *
 * @author Maxim Butov
 */
public class CompoundCopyPolicy implements CopyPolicy {

    private final CopyPolicy[] policies;

    /**
     * Creates copy policy.
     *
     * @param policies list of policies
     */
    public CompoundCopyPolicy(List<CopyPolicy> policies) {
        this.policies = policies.toArray(new CopyPolicy[0]);
    }

    /**
     * Creates copy policy.
     *
     * @param policies list of policies
     */
    public CompoundCopyPolicy(CopyPolicy... policies) {
        this.policies = policies;
    }

    private <A extends Enum<A>> A findAction(Function<CopyPolicy, A> mapping, A defaultAction) {
        for (CopyPolicy policy : policies) {
            A action = mapping.apply(policy);
            if (action != defaultAction) {
                return action;
            }
        }
        return defaultAction;
    }

    private <A extends Enum<A>> A findAction0(Function<CopyPolicy, A> mapping, A defaultAction) {
        return Arrays.stream(policies)
            .map(mapping)
            .filter(action -> action != defaultAction)
            .findFirst()
            .orElse(defaultAction);
    }

    @Override
    public CopyAction getTypeAction(Class<?> type) {
        return findAction0(policy -> policy.getTypeAction(type), CopyAction.DEFAULT);
    }

    @Override
    public FieldCopyAction getFieldAction(Field field) {
        return findAction0(policy -> policy.getFieldAction(field), FieldCopyAction.DEFAULT);
    }

}
