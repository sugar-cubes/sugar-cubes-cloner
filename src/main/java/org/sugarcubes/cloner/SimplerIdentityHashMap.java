package org.sugarcubes.cloner;

import java.util.AbstractMap;
import java.util.Set;

class SimplerIdentityHashMap<K, V> extends AbstractMap<K, V> {

    Object[] table;
    int size;

    SimplerIdentityHashMap() {
        this(32);
    }

    SimplerIdentityHashMap(int expectedMaxSize) {
        int initCapacity = Integer.highestOneBit(expectedMaxSize + (expectedMaxSize << 1));
        table = new Object[2 * initCapacity];
    }

    static int hash(Object x, int length) {
        int h = System.identityHashCode(x);
        return ((h << 1) - (h << 8)) & (length - 1);
    }

    static int nextKeyIndex(int i, int len) {
        return (i + 2 < len ? i + 2 : 0);
    }

    @SuppressWarnings("unchecked")
    public V get(Object key) {
        Object[] tab = table;
        int len = tab.length;
        int i = hash(key, len);
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

    public boolean containsKey(Object key) {
        Object[] tab = table;
        int len = tab.length;
        int i = hash(key, len);
        while (true) {
            Object item = tab[i];
            if (item == key) {
                return true;
            }
            if (item == null) {
                return false;
            }
            i = nextKeyIndex(i, len);
        }
    }

    public V put(K key, V value) {
        retryAfterResize:
        for (; ; ) {
            Object[] tab = table;
            int len = tab.length;
            int i = hash(key, len);

            for (Object item; (item = tab[i]) != null;
                i = nextKeyIndex(i, len)) {
                if (item == key) {
                    @SuppressWarnings("unchecked")
                    V oldValue = (V) tab[i + 1];
                    tab[i + 1] = value;
                    return oldValue;
                }
            }

            int s = size + 1;
            if (s + (s << 1) > len && resize(len)) {
                continue retryAfterResize;
            }

            tab[i] = key;
            tab[i + 1] = value;
            size = s;
            return null;
        }
    }

    boolean resize(int newCapacity) {
        int newLength = newCapacity * 2;

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
                oldTable[j] = null;
                oldTable[j + 1] = null;
                int i = hash(key, newLength);
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
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

}
