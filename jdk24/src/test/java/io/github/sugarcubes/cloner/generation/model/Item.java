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
package io.github.sugarcubes.cloner.generation.model;

import java.util.ArrayList;
import java.util.List;

public class Item {

    private final long id;

    private String name;

    private double price;

    private final List<String> tags = new ArrayList<>();

    private Item next;

    private final Item owner;

    public Item(long id, String name, double price, Item owner) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.owner = owner;
        tags.add("tag" + id);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public List<String> getTags() {
        return tags;
    }

    public Item getNext() {
        return next;
    }

    public void setNext(Item next) {
        this.next = next;
    }

    public Item getOwner() {
        return owner;
    }

}
