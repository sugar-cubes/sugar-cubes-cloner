package org.sugarcubes.cloner.thirdparty;

import java.util.function.Supplier;

import org.objenesis.strategy.StdInstantiatorStrategy;
import org.sugarcubes.cloner.AbstractCloner;

import com.esotericsoftware.kryo.Kryo;

/**
 * The implementation of {@link org.sugarcubes.cloner.Cloner} which uses Kryo serialization for cloning.
 *
 * @see <a href="https://github.com/EsotericSoftware/kryo">Kryo</a>
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
