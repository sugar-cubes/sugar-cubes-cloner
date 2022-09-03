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

    /**
     * List of policies.
     */
    private final CopyPolicy[] policies;

    /**
     * Creates copy policy.
     *
     * @param policies list of policies
     */
    public CompoundCopyPolicy(List<CopyPolicy> policies) {
        this.policies = policies.toArray(new CopyPolicy[0]);
    }

    private <A extends Enum<A>> A findAction(Function<CopyPolicy, A> mapping, A defaultAction) {
        return Arrays.stream(policies)
            .map(mapping)
            .filter(action -> action != defaultAction)
            .findFirst()
            .orElse(defaultAction);
    }

    @Override
    public CopyAction getTypeAction(Class<?> type) {
        return findAction(policy -> policy.getTypeAction(type), CopyAction.DEFAULT);
    }

    @Override
    public FieldCopyAction getFieldAction(Field field) {
        return findAction(policy -> policy.getFieldAction(field), FieldCopyAction.DEFAULT);
    }

}
