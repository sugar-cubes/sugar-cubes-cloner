package org.sugarcubes.cloner;

import java.util.AbstractMap;
import java.util.Set;

/**
 * Minimalistic {@link java.util.IdentityHashMap} with no nulls allowed, maybe bit faster.
 *
 * @author Doug Lea and Josh Bloch
 */
public class FasterIdentityHashMap<K, V> extends AbstractMap<K, V> {

    /**
     * Default capacity.
     */
    private static final int DEFAULT_CAPACITY = 32;

    private Object[] table;
    private int size;

    /**
     * Creates map with default capacity.
     */
    public FasterIdentityHashMap() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Creates map with specified capacity.
     *
     * @param expectedMaxSize expected maximum size
     */
    public FasterIdentityHashMap(int expectedMaxSize) {
        table = new Object[capacity(expectedMaxSize) << 1];
    }

    private static int capacity(int expectedMaxSize) {
        int twoPow = Integer.highestOneBit(expectedMaxSize);
        return twoPow == expectedMaxSize ? twoPow : twoPow << 1;
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private static int hash(int h, int length) {
        return ((h << 1) - (h << 8)) & (length - 1);
    }

    private static int nextKeyIndex(int i, int len) {
        return (i + 2 < len ? i + 2 : 0);
    }

    @Override
    public V get(Object key) {
        Object[] tab = table;
        int len = tab.length;
        int i = hash(System.identityHashCode(key), len);
        while (true) {
            Object item = tab[i];
            if (item == key) {
                return (V) tab[i + 1];
            }
            if (item == null) {
                return null;
            }
            i = nextKeyIndex(i, len);
        }
    }

    @Override
    public V put(K key, V value) {
        int h = System.identityHashCode(key);
        while (true) {
            Object[] tab = table;
            int len = tab.length;
            int i = hash(h, len);

            for (Object item; (item = tab[i]) != null; i = nextKeyIndex(i, len)) {
                if (item == key) {
                    V oldValue = (V) tab[i + 1];
                    tab[i + 1] = value;
                    return oldValue;
                }
            }

            int s = size + 1;
            if (s + (s << 1) > len && resize(len)) {
                continue;
            }

            tab[i] = key;
            tab[i + 1] = value;
            size = s;
            return null;
        }
    }

    private boolean resize(int newCapacity) {
        int newLength = newCapacity << 1;

        Object[] oldTable = table;
        int oldLength = oldTable.length;
        if (oldLength >= newLength) {
            return false;
        }

        Object[] newTable = new Object[newLength];

        for (int j = 0; j < oldLength; j += 2) {
            Object key = oldTable[j];
            if (key != null) {
                Object value = oldTable[j + 1];
                int i = hash(System.identityHashCode(key), newLength);
                while (newTable[i] != null) {
                    i = nextKeyIndex(i, newLength);
                }
                newTable[i] = key;
                newTable[i + 1] = value;
            }
        }
        table = newTable;
        return true;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

}
