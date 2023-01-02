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

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * The implementation of {@link Cloner} which uses Java serialization for cloning.
 *
 * @author Maxim Butov
 */
public class SerializationCloner implements Cloner {

    /**
     * Singleton instance of the cloner.
     */
    public static final Cloner INSTANCE = new SerializationCloner();

    /**
     * A faster implementation.
     */
    static final class ByteArrayOutputStream extends java.io.ByteArrayOutputStream {

        static final int SIZE = 0x1000;

        ByteArrayOutputStream() {
            super(SIZE);
        }

        byte[] buf() {
            return buf;
        }

    }

    private static <T> T serializeDeserialize(T object) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new ObjectOutputStream(out).writeObject(object);
        return (T) new ObjectInputStream(new ByteArrayInputStream(out.buf(), 0, out.size())).readObject();
    }

    @Override
    public <T> T clone(T object) {
        return ClonerExceptionUtils.replaceException(() -> serializeDeserialize(object));
    }

}
