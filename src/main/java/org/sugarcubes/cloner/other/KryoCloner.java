package org.sugarcubes.cloner.other;

import java.util.function.Supplier;

import org.objenesis.strategy.StdInstantiatorStrategy;
import org.sugarcubes.cloner.AbstractCloner;
import org.sugarcubes.cloner.Cloner;

import com.esotericsoftware.kryo.Kryo;

/**
 * The implementation of {@link Cloner} which uses Kryo serialization for cloning.
 *
 * @author Maxim Butov
 */
public class KryoCloner extends AbstractCloner {

    private final Supplier<Kryo> kryo;

    public KryoCloner(Supplier<Kryo> kryo) {
        this.kryo = kryo;
    }

    public KryoCloner(Kryo kryo) {
        this(() -> kryo);
    }

    public KryoCloner() {
        this(
            () -> {
                Kryo kryo = new Kryo();
                kryo.setRegistrationRequired(false);
                kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
                return kryo;
            }
        );
    }

    @Override
    protected Object doClone(Object object) throws Throwable {
        return kryo.get().copy(object);
    }

}
