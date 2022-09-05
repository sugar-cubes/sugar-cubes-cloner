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
 * Generic cloner exception. Wraps all exception during cloning process.
 *
 * @author Maxim Butov
 */
public class ClonerException extends RuntimeException {

    /**
     * Constructor with message and cause.
     *
     * @param message message
     * @param cause original exception
     */
    public ClonerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with cause.
     *
     * @param cause original exception
     */
    public ClonerException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with message.
     *
     * @param message message
     */
    public ClonerException(String message) {
        super(message);
    }

}
