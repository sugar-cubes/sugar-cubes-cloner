package org.sugarcubes.cloner.thirdparty;

import org.sugarcubes.cloner.AbstractCloner;

import com.rits.cloning.Cloner;

/**
 * The implementation of {@link org.sugarcubes.cloner.Cloner} which delegates to {@link com.rits.cloning.Cloner}..
 *
 * @see <a href="https://github.com/kostaskougios/cloning">cloning</a>
 *
 * @author Maxim Butov
 */
public class KKCloner extends AbstractCloner {

    /**
     * The actual cloner.
     */
    private final com.rits.cloning.Cloner delegate;

    public KKCloner() {
        this(new com.rits.cloning.Cloner());
    }

    public KKCloner(com.rits.cloning.Cloner delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns the actual cloner.
     *
     * @return actual cloner
     */
    public Cloner getDelegate() {
        return delegate;
    }

    @Override
    protected Object doClone(Object object) throws Throwable {
        return delegate.deepClone(object);
    }

}
