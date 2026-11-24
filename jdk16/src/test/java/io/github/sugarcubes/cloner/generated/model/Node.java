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
package io.github.sugarcubes.cloner.generated.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A tree whose children reference their parent by a final field: a cycle through a final field.
 */
public class Node {

    private final Node parent;

    private final List<Node> children = new ArrayList<>();

    private final String name;

    public Node(Node parent, String name) {
        this.parent = parent;
        this.name = name;
        if (parent != null) {
            parent.children.add(this);
        }
    }

    public Node getParent() {
        return parent;
    }

    public List<Node> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

}
