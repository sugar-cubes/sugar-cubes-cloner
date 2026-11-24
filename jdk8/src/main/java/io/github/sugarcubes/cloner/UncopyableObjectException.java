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
package io.github.sugarcubes.cloner;

import java.util.ArrayList;
import java.util.List;

/**
 * Thrown when the cloner meets a live object of the JVM or the operating system (a thread, a reference, an open file,
 * a socket, a process, an executor) and the developer has not chosen what the clone gets: a clone cannot get its own
 * one, and sharing the original must be chosen explicitly by {@link CopyAction#ORIGINAL} (or {@link CopyAction#NULL})
 * for the type, the field or the object, or by a copier of the type.
 *
 * <p>The message tells the classes of the objects holding the uncopyable one, nearest first, as far as they are
 * known: in the sequential mode only the nearest one.</p>
 *
 * @author Maxim Butov
 */
public class UncopyableObjectException extends ClonerException {

    private static final long serialVersionUID = 1L;

    /**
     * Number of holders in the message, the farther ones are omitted.
     */
    private static final int MAX_HOLDERS = 8;

    /**
     * Type of the uncopyable object.
     */
    private final Class<?> type;

    /**
     * Classes of the objects holding the uncopyable one, nearest first.
     */
    private final List<Class<?>> holders = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param type type of the uncopyable object
     */
    public UncopyableObjectException(Class<?> type) {
        super(type.getName());
        this.type = type;
    }

    /**
     * Returns the type of the uncopyable object.
     *
     * @return type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Adds the class of an object holding the uncopyable one, farther than the added before.
     *
     * @param holder class of the holding object
     */
    public void addHolder(Class<?> holder) {
        holders.add(holder);
    }

    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder("Cannot copy ").append(type.getName());
        int shown = 0;
        for (int k = 0; k < holders.size(); k++) {
            if (k > 0 && holders.get(k) == holders.get(k - 1)) {
                // e.g. nodes of a linked list
                continue;
            }
            if (shown == MAX_HOLDERS) {
                message.append(" ...");
                break;
            }
            message.append(" in ").append(holders.get(k).getName());
            shown++;
        }
        return message.append(": threads, references, open files, sockets, processes and executors are not copied. "
            + "Choose CopyAction.ORIGINAL to share it or CopyAction.NULL for the type (typeAction), the field "
            + "(fieldAction) or the object (objectAction), or set a copier for the type.").toString();
    }

}
