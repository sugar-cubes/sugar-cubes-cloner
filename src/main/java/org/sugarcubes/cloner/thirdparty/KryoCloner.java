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

    /**
     * {@link Kryo} factory.
     */
    private final Supplier<Kryo> kryo;

    /**
     * Constructor.
     */
    public KryoCloner(Supplier<Kryo> kryo) {
        this.kryo = kryo;
    }

    /**
     * Constructor.
     */
    public KryoCloner(Kryo kryo) {
        this(() -> kryo);
    }

    /**
     * Constructor.
     */
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
