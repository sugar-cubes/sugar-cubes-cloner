package org.sugarcubes.cloner;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * The implementation of {@link Cloner} which uses Java serialization for cloning.
 *
 * @author Maxim Butov
 */
public class SerializationCloner extends AbstractCloner {

    /**
     * Singleton instance of the cloner.
     */
    public static final Cloner INSTANCE = new SerializationCloner();

    /**
     * A faster implementation.
     */
    static class ByteArrayOutputStream extends java.io.ByteArrayOutputStream {

        static final int SIZE = 0x1000;

        ByteArrayOutputStream() {
            super(SIZE);
        }

        byte[] buf() {
            return buf;
        }

    }

    @Override
    protected Object doClone(Object object) throws Throwable {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new ObjectOutputStream(out).writeObject(object);
        return new ObjectInputStream(new ByteArrayInputStream(out.buf(), 0, out.size())).readObject();
    }

}
