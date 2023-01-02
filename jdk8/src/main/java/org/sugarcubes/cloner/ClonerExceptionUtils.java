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
package org.sugarcubes.cloner;

/**
 * Exception handling utilities.
 *
 * @author Maxim Butov
 */
public class ClonerExceptionUtils {

    /**
     * A variant of Callable.
     *
     * @param <T> result type
     */
    public interface Action<T> {

        /**
         * Actual work.
         *
         * @return result
         * @throws Throwable if something went wrong
         */
        T perform() throws Throwable;

    }

    /**
     * Performs action and replaces exception (if any) with {@link ClonerException}.
     *
     * @param <T> result type
     * @param action action
     * @return result
     * @throws ClonerException if something went wrong
     */
    public static <T> T replaceException(Action<T> action) throws ClonerException {
        try {
            return action.perform();
        }
        catch (ClonerException | Error e) {
            throw e;
        }
        catch (Throwable e) {
            throw new ClonerException(e);
        }
    }

    /**
     * Utility class.
     */
    private ClonerExceptionUtils() {
    }
}
