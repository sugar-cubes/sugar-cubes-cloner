/*
 * Copyright 2017-2023 the original author or authors.
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
 * Java Reflection Cloner library.
 * <p>Default configuration example:</p>
 * <pre>
 *     Cloner cloner = Cloners.reflection();
 *     SomeObject myObjectClone = cloner.clone(myObject);
 * </pre>
 * <p>Custom configuration example:</p>
 * <pre>
 *     Cloner cloner =
 *         // new builder instance
 *         Cloners.builder()
 *             // custom allocator
 *             .objectFactoryProvider(new ObjenesisObjectFactoryProvider())
 *             // copy thread locals by reference
 *             .typeAction(Predicates.subclass(ThreadLocal.class), CopyAction.ORIGINAL)
 *             // do not clone closeables
 *             .typeAction(Predicates.subclass(AutoCloseable.class), CopyAction.ORIGINAL)
 *             // skip SomeObject.cachedValue field when cloning
 *             .fieldAction(SomeObject.class, "cachedValue", CopyAction.SKIP)
 *             // set fields with @Transient annotation to null
 *             .fieldAction(Predicates.annotatedWith(Transient.class), CopyAction.NULL)
 *             // custom copier for SomeOtherObject type
 *             .copier(CustomObject.class, new CustomObjectCopier())
 *             // parallel mode
 *             .mode(CloningMode.PARALLEL)
 *             // create cloner
 *             .build();
 *
 *         // perform cloning
 *         SomeObject clone = cloner.clone(original);
 * </pre>
 *
 * @see org.sugarcubes.cloner.Cloner
 * @see org.sugarcubes.cloner.ClonerException
 * @see org.sugarcubes.cloner.Cloners
 * @see org.sugarcubes.cloner.CloningMode
 * @see org.sugarcubes.cloner.CopyAction
 * @see org.sugarcubes.cloner.FieldPolicy
 * @see org.sugarcubes.cloner.ReflectionClonerBuilder
 * @see org.sugarcubes.cloner.TraversalAlgorithm
 * @see org.sugarcubes.cloner.TypeCopier
 * @see org.sugarcubes.cloner.TypePolicy
 *
 * @author Maxim Butov
 */
package org.sugarcubes.cloner;
