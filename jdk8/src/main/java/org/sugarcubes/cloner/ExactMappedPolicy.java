package org.sugarcubes.cloner;

import java.util.Map;
import java.util.function.Function;

/**
 * Copy policy based on exact values.
 *
 * @see PredicatePolicy
 *
 * @author Maxim Butov
 */
public class ExactMappedPolicy<I> extends AbstractMappedPolicy<I, I> {

    /**
     * Creates policy.
     *
     * @param map map (input, action)
     */
    public ExactMappedPolicy(Map<I, CopyAction> map) {
        super(map);
    }

    /**
     * Creates policy.
     *
     * @param map map (input, action)
     * @param mapCopyConstructor copy constructor of map as method reference
     */
    public ExactMappedPolicy(Map<I, CopyAction> map, Function<Map<I, CopyAction>, Map<I, CopyAction>> mapCopyConstructor) {
        super(map, mapCopyConstructor);
    }

    @Override
    public CopyAction getAction(I input) {
        return get(input);
    }

}
