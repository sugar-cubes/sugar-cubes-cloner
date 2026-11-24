/*
 * Copyright 2017-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.sugarcubes.cloner.tools.samples;

public final class Node {

    private final int value;

    private final Node first;

    private Node next;

    private Node(int value, Node first) {
        this.value = value;
        this.first = first != null ? first : this;
    }

    public static Node sample() {
        Node first = new Node(1, null);
        Node second = new Node(2, first);
        first.next = second;
        second.next = first;
        return second;
    }

    @Override
    public String toString() {
        return value + " -> " + next.value + ", first " + first.value + ", ring " + (next.next == this)
            + ", first is next " + (first == next);
    }

}
