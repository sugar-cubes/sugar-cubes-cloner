package org.sugarcubes.cloner;

import java.util.EnumMap;
import java.util.Map;

/**
 * Copier for {@link EnumMap}.
 *
 * @author Maxim Butov
 */
public class EnumMapCopier<K extends Enum<K>, V> extends TwoPhaseObjectCopier<EnumMap<K, V>> {

    @Override
    public EnumMap<K, V> allocate(EnumMap<K, V> original) throws Exception {
        return original.clone();
    }

    @Override
    public void deepCopy(EnumMap<K, V> original, EnumMap<K, V> clone, CopyContext context) throws Exception {
        for (Map.Entry<K, V> entry : clone.entrySet()) {
            entry.setValue(context.copy(entry.getValue()));
        }
    }

}
