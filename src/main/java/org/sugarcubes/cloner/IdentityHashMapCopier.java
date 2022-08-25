package org.sugarcubes.cloner;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Copier for {@link IdentityHashMap}.
 *
 * {@link ReflectionCopier} is not suitable for {@link IdentityHashMap} because the result's {@link IdentityHashMap#table} has its
 * entries on wrong places because clones have different identity hash codes than original objects. Thus, the map has to be
 * entirely rebuilt.
 *
 * @author Maxim Butov
 */
public class IdentityHashMapCopier extends TwoPhaseObjectCopier<IdentityHashMap<Object, Object>> {

    @Override
    public IdentityHashMap<Object, Object> allocate(IdentityHashMap<Object, Object> original) throws Exception {
        return new IdentityHashMap<>(original.size());
    }

    @Override
    public void deepCopy(IdentityHashMap<Object, Object> original, IdentityHashMap<Object, Object> clone, CopyContext context)
        throws Exception {
        for (Map.Entry<Object, Object> entry : original.entrySet()) {
            clone.put(context.copy(entry.getKey()), context.copy(entry.getValue()));
        }
    }

}
