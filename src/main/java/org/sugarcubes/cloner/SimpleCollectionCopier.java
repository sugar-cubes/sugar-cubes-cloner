package org.sugarcubes.cloner;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

/**
 * Copier for simple collections (that do not call items {@link Object#hashCode()} method), because item hash code can differ in
 * the beginning and in the end of cloning.
 *
 * @author Maxim Butov
 */
public class SimpleCollectionCopier<T extends Collection<Object>> extends TwoPhaseObjectCopier<T> {

    /**
     * Collection constructor with size argument.
     */
    private final Function<Integer, T> constructor;

    /**
     * Creates copier.
     *
     * @param constructor collection constructor
     */
    public SimpleCollectionCopier(Function<Integer, T> constructor) {
        this.constructor = constructor;
    }

    @Override
    public T allocate(T original) throws Exception {
        return constructor.apply(original.size());
    }

    @Override
    public void deepCopy(T original, T clone, CopyContext context) throws Exception {
        Object[] array = original.toArray();
        for (int k = 0, length = array.length; k < length; k++) {
            array[k] = context.copy(array[k]);
        }
        clone.addAll(Arrays.asList(array));
    }

}
