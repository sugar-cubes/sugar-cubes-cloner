/*
 * Copyright 2017-2024 the original author or authors.
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
package io.github.sugarcubes.cloner.model;

import java.util.ArrayList;
import java.util.List;

public class Order {

    public enum Status { NEW, PAID, SHIPPED }

    private final long id;
    private String number;
    private long created;
    private double total;
    private Status status = Status.NEW;
    private Customer customer;
    private Address delivery;
    private final List<Line> lines = new ArrayList<>();

    public Order(long id, String number, long created, Customer customer, Address delivery) {
        this.id = id;
        this.number = number;
        this.created = created;
        this.customer = customer;
        this.delivery = delivery;
    }

    public void add(Product product, int quantity, double price) {
        lines.add(new Line(this, product, quantity, price, 0.1f));
        total += quantity * price;
    }

}
