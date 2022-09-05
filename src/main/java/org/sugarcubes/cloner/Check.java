/**
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

/**
 * Arguments checking utilities.
 *
 * @author Maxim Butov
 */
public class Check {

    /**
     * Throws {@link IllegalArgumentException}.
     *
     * @param format format for exception message
     * @param args arguments for exception message
     * @return nothing, always throws exception
     */
    public static Error illegalArg(String format, Object... args) throws IllegalArgumentException {
        throw new IllegalArgumentException(args.length != 0 ? String.format(format, args) : format);
    }

    /**
     * Throws {@link IllegalArgumentException} if condition is true.
     *
     * @param condition condition
     * @param format format for exception message
     * @param args arguments for exception message
     */
    public static void illegalArg(boolean condition, String format, Object... args) throws IllegalArgumentException {
        if (condition) {
            throw illegalArg(format, args);
        }
    }

    /**
     * Checks the value is not null or throws exception.
     *
     * @param <T> object type
     * @param value value
     * @param format format for exception message
     * @param args arguments for exception message
     * @return value
     * @throws IllegalArgumentException if {@code value == null}
     */
    public static <T> T notNull(T value, String format, Object... args) throws IllegalArgumentException {
        illegalArg(value == null, format, args);
        return value;
    }

    /**
     * Checks the value is not null or throws exception.
     *
     * @param <T> object type
     * @param value value
     * @param name argument name
     * @return value
     * @throws IllegalArgumentException if {@code value == null}
     */
    public static <T> T argNotNull(T value, String name) throws IllegalArgumentException {
        return notNull(value, "%s must be not null.", name);
    }

    /**
     * Checks the value is null or throws exception.
     *
     * @param value value
     * @param format format for exception message
     * @param args arguments for exception message
     * @throws IllegalArgumentException if {@code value != null}
     */
    public static void isNull(Object value, String format, Object... args) throws IllegalArgumentException {
        illegalArg(value != null, format, args);
    }

    /**
     * Utility class.
     */
    private Check() {
    }

}
