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
import java.util.Arrays;
import java.util.Set;

/**
 * Minimalistic {@link java.util.IdentityHashMap} with no nulls allowed, maybe bit faster.
 *
 * @author Doug Lea and Josh Bloch
 */
public final class FasterIdentityHashMap<K, V> extends AbstractMap<K, V> {

    private Object[] table;
    private int size;

    public FasterIdentityHashMap() {
        this(32);
    }

    public FasterIdentityHashMap(int expectedMaxSize) {
        table = new Object[capacity(expectedMaxSize) << 1];
    }

    private static int capacity(int expectedMaxSize) {
        int hob = Integer.highestOneBit(expectedMaxSize);
        return hob == expectedMaxSize ? hob : hob << 1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

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

            for (Object item; (item = tab[i]) != null;
                i = nextKeyIndex(i, len)) {
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
    public void clear() {
        Arrays.fill(table, null);
        size = 0;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

}
