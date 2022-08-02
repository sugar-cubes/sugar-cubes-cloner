package org.sugarcubes.cloner;

import java.util.Collection;
import java.util.function.Function;

/**
 * @author Maxim Butov
 */
public class SimpleCollectionCopier<T extends Collection<Object>> extends TwoPhaseObjectCopier<T> {

    private final Function<Integer, T> constructor;

    public SimpleCollectionCopier(Function<Integer, T> constructor) {
        this.constructor = constructor;
    }

    @Override
    public T allocate(T original) throws Throwable {
        return constructor.apply(original.size());
    }

    @Override
    public void deepCopy(T original, T clone, CopyContext context) throws Throwable {
        for (Object value : original) {
            clone.add(context.copy(value));
        }
    }

}
