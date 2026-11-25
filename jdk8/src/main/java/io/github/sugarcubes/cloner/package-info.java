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
/**
 * Java deep cloning library.
 *
 * <p>Clone with the default cloner:</p>
 * <pre>
 *     Order clone = Cloners.clone(order);
 * </pre>
 *
 * <p>Configure a cloner once and reuse it, cloners are thread safe:</p>
 * <pre>
 *     Cloner cloner = Cloners.builder()
 *         // share instances of a type and its subtypes
 *         .typeAction(Predicates.subclass(Connection.class), CopyAction.ORIGINAL)
 *         // do not copy a field
 *         .fieldAction(Order.class, "cachedTotal", CopyAction.SKIP)
 *         // set fields with an annotation to null
 *         .fieldAction(Predicates.annotatedWith(Transient.class), CopyAction.NULL)
 *         // a custom copier of a type
 *         .copier(Money.class, new MoneyCopier())
 *         // copy with the threads of a pool
 *         .executor(pool)
 *         .build();
 *
 *     Order clone = cloner.clone(order);
 * </pre>
 *
 * <p>Objects which cannot be copied meaningfully (threads, open files, sockets, executors) make cloning fail with
 * {@link io.github.sugarcubes.cloner.UncopyableObjectException} unless the configuration decides what the clone gets.
 * The low-level mechanisms (allocation, field access, module opening) are chosen by
 * {@link io.github.sugarcubes.cloner.Toolsets#defaults()} for the running JVM, see
 * {@link io.github.sugarcubes.cloner.Toolset}.</p>
 *
 * <p>The guide: <a href="https://sugar-cubes.github.io/sugar-cubes-cloner/">sugar-cubes.github.io/sugar-cubes-cloner</a>.</p>
 *
 * @see io.github.sugarcubes.cloner.Cloners
 * @see io.github.sugarcubes.cloner.ReflectionClonerBuilder
 * @see io.github.sugarcubes.cloner.CopyAction
 * @see io.github.sugarcubes.cloner.Toolsets
 *
 * @author Maxim Butov
 */
package io.github.sugarcubes.cloner;
