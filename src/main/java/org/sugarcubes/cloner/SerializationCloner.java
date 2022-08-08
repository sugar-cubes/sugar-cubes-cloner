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
        try {
            return serializeDeserialize(object);
        }
        catch (Exception e) {
            throw new ClonerException(e);
        }
    }

}
