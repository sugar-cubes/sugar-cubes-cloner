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
package io.github.sugarcubes.cloner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import io.github.sugarcubes.cloner.model.Address;
import io.github.sugarcubes.cloner.model.Customer;
import io.github.sugarcubes.cloner.model.Order;
import io.github.sugarcubes.cloner.model.Product;

/**
 * Copiers by fields against generated copiers on a graph of own classes: orders, lines, products, customers.
 *
 * @author Maxim Butov
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(value = 2)
@Warmup(iterations = 3, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@SuppressWarnings("checkstyle:all")
public class GeneratedCopiersBenchmark {

    @Param({"large", "small"})
    private String size;

    private Cloner reflection;
    private Cloner generated;

    private Object sample;

    @Setup
    public void setup() {
        reflection = Cloners.builder().generatedCopiers(false).build();
        generated = Cloners.builder().generatedCopiers(true).build();
        int orders = "large".equals(size) ? 1000 : 1;
        int productCount = 100;
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < productCount; i++) {
            products.add(new Product("sku" + i, "product " + i, i * 0.1, i, new String[] {"a" + i % 5, "b" + i % 7}));
        }
        List<Order> list = new ArrayList<>();
        for (int i = 0; i < orders; i++) {
            Address address = new Address("street " + i, "city " + i % 10, 10000 + i, i * 0.01, i * 0.02);
            Customer customer = new Customer(i, "customer " + i, "c" + i + "@mail", i % 3 == 0, address);
            Order order = new Order(i, "N" + i, 1_700_000_000_000L + i, customer, address);
            for (int j = 0; j < 10; j++) {
                order.add(products.get((i * 7 + j) % productCount), j + 1, 9.99 + j);
            }
            list.add(order);
        }
        sample = list;
    }

    @Benchmark
    public Object reflection() {
        return reflection.clone(sample);
    }

    @Benchmark
    public Object generated() {
        return generated.clone(sample);
    }

}
