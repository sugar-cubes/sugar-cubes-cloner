/*
 * Copyright 2017-2022 the original author or authors.
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
package org.sugarcubes.cloner;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.function.Predicate;

/**
 * Predicates factory. To be used with {@link PredicatePolicy}.
 *
 * @see PredicatePolicy
 *
 * @author Maxim Butov
 */
public class Predicates {

    /**
     * Negates predicate.
     *
     * @param <T> object type
     * @param predicate predicate
     * @return negated predicate
     */
    public static <T> Predicate<T> not(Predicate<T> predicate) {
        return predicate.negate();
    }

    /**
     * Predicate which test an object is equal to reference value.
     *
     * @param <T> object type
     * @param referenceValue reference value
     * @return predicate
     */
    public static <T> Predicate<T> equal(T referenceValue) {
        return Predicate.isEqual(referenceValue);
    }

    /**
     * Predicate which test an object is same (==) as reference value.
     *
     * @param <T> object type
     * @param referenceValue reference value
     * @return predicate
     */
    public static <T> Predicate<T> same(T referenceValue) {
        return t -> t == referenceValue;
    }

    /**
     * Predicate which test an element is annotated with annotation.
     *
     * @param <T> element type
     * @param <A> annotation type
     * @param annotation annotation type
     * @return predicate
     */
    public static <T extends AnnotatedElement, A extends Annotation> Predicate<T> annotatedWith(Class<A> annotation) {
        return element -> element.getDeclaredAnnotation(annotation) != null;
    }

    /**
     * Predicate which test a class is subclass (including self) of the type.
     *
     * @param type type
     * @return predicate
     */
    public static Predicate<Class<?>> subclass(Class<?> type) {
        return type::isAssignableFrom;
    }

    /**
     * Utility class.
     */
    private Predicates() {
    }

}
