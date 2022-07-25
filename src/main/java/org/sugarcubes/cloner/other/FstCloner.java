package org.sugarcubes.cloner.other;

import org.nustaq.serialization.simpleapi.DefaultCoder;
import org.nustaq.serialization.simpleapi.FSTCoder;
import org.sugarcubes.cloner.AbstractCloner;
import org.sugarcubes.cloner.Cloner;

/**
 * The implementation of {@link Cloner} which uses FST serialization for cloning.
 *
 * @author Maxim Butov
 */
public class FstCloner extends AbstractCloner {

    private final FSTCoder coder;

    public FstCloner() {
        this(new DefaultCoder());
    }

    public FstCloner(FSTCoder coder) {
        this.coder = coder;
    }

    @Override
    protected Object doClone(Object object) throws Throwable {
        return coder.toObject(coder.toByteArray(object));
    }

}
