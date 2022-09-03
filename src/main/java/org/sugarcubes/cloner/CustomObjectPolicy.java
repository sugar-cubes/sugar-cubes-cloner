package org.sugarcubes.cloner;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Implementation of {@link ObjectPolicy}.
 *
 * @author Maxim Butov
 */
public class CustomObjectPolicy implements ObjectPolicy {

    /**
     * Map (object, action).
     */
    private final Map<Object, CopyAction> objectActions;

    /**
     * Creates an instance.
     *
     * @param objectActions map (object, action)
     */
    public CustomObjectPolicy(Map<Object, CopyAction> objectActions) {
        this.objectActions = new IdentityHashMap<>(objectActions);
        this.objectActions.entrySet().removeIf(entry -> entry.getValue() == CopyAction.DEFAULT);
        Check.illegalArg(this.objectActions.isEmpty(), "Action map must contain at least one non-default action.");
    }

    @Override
    public CopyAction getObjectAction(Object original) {
        return objectActions.getOrDefault(original, CopyAction.DEFAULT);
    }

}
