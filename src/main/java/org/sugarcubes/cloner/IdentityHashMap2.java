/*
 * Copyright (c) 2000, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.sugarcubes.cloner;

import java.util.AbstractMap;
import java.util.Set;

public class IdentityHashMap2<K, V> extends AbstractMap<K, V> {

    Object[] table; // non-private to simplify nested class access

    /**
     * The number of key-value mappings contained in this identity hash map.
     *
     * @serial
     */
    int size;

    /**
     * Constructs a new, empty identity hash map with a default expected
     * maximum size (21).
     */
    public IdentityHashMap2() {
        init(32);
    }

    /**
     * Constructs a new, empty map with the specified expected maximum size.
     * Putting more than the expected number of key-value mappings into
     * the map may cause the internal data structure to grow, which may be
     * somewhat time-consuming.
     *
     * @param expectedMaxSize the expected maximum size of the map
     * @throws IllegalArgumentException if <tt>expectedMaxSize</tt> is negative
     */
    public IdentityHashMap2(int expectedMaxSize) {
        init(capacity(expectedMaxSize));
    }

    private static int capacity(int expectedMaxSize) {
        return Integer.highestOneBit(expectedMaxSize + (expectedMaxSize << 1));
    }

    /**
     * Initializes object to be an empty map with the specified initial
     * capacity, which is assumed to be a power of two between
     * MINIMUM_CAPACITY and MAXIMUM_CAPACITY inclusive.
     */
    private void init(int initCapacity) {
        table = new Object[2 * initCapacity];
    }

    /**
     * Returns index for Object x.
     */
    private static int hash(Object x, int length) {
        int h = System.identityHashCode(x);
        // Multiply by -127, and left-shift to use least bit as part of hash
        return ((h << 1) - (h << 8)) & (length - 1);
    }

    /**
     * Circularly traverses table of size len.
     */
    private static int nextKeyIndex(int i, int len) {
        return (i + 2 < len ? i + 2 : 0);
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key == k)},
     * then this method returns {@code v}; otherwise it returns
     * {@code null}.  (There can be at most one such mapping.)
     *
     * <p>A return value of {@code null} does not <i>necessarily</i>
     * indicate that the map contains no mapping for the key; it's also
     * possible that the map explicitly maps the key to {@code null}.
     * The {@link #containsKey containsKey} operation may be used to
     * distinguish these two cases.
     *
     * @see #put(Object, Object)
     */
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        Object k = key;
        Object[] tab = table;
        int len = tab.length;
        int i = hash(k, len);
        while (true) {
            Object item = tab[i];
            if (item == k) {
                return (V) tab[i + 1];
            }
            if (item == null) {
                return null;
            }
            i = nextKeyIndex(i, len);
        }
    }

    /**
     * Tests whether the specified object reference is a key in this identity
     * hash map.
     *
     * @param   key   possible key
     * @return  <code>true</code> if the specified object reference is a key
     *          in this map
     * @see     #containsValue(Object)
     */
    public boolean containsKey(Object key) {
        Object k = key;
        Object[] tab = table;
        int len = tab.length;
        int i = hash(k, len);
        while (true) {
            Object item = tab[i];
            if (item == k) {
                return true;
            }
            if (item == null) {
                return false;
            }
            i = nextKeyIndex(i, len);
        }
    }

    /**
     * Associates the specified value with the specified key in this identity
     * hash map.  If the map previously contained a mapping for the key, the
     * old value is replaced.
     *
     * @param key the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     * @see     Object#equals(Object)
     * @see     #get(Object)
     * @see     #containsKey(Object)
     */
    public V put(K key, V value) {
        final Object k = key;

        retryAfterResize:
        for (; ; ) {
            final Object[] tab = table;
            final int len = tab.length;
            int i = hash(k, len);

            for (Object item; (item = tab[i]) != null;
                i = nextKeyIndex(i, len)) {
                if (item == k) {
                    @SuppressWarnings("unchecked")
                    V oldValue = (V) tab[i + 1];
                    tab[i + 1] = value;
                    return oldValue;
                }
            }

            final int s = size + 1;
            // Use optimized form of 3 * s.
            // Next capacity is len, 2 * current capacity.
            if (s + (s << 1) > len && resize(len)) {
                continue retryAfterResize;
            }

            tab[i] = k;
            tab[i + 1] = value;
            size = s;
            return null;
        }
    }

    /**
     * Resizes the table if necessary to hold given capacity.
     *
     * @param newCapacity the new capacity, must be a power of two.
     * @return whether a resize did in fact take place
     */
    private boolean resize(int newCapacity) {
        // assert (newCapacity & -newCapacity) == newCapacity; // power of 2
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
