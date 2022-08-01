package org.sugarcubes.cloner.other;

import org.sugarcubes.cloner.AbstractCloner;

import com.rits.cloning.Cloner;

public class KKCloner extends AbstractCloner {

    private final com.rits.cloning.Cloner delegate = new com.rits.cloning.Cloner();

    public Cloner getDelegate() {
        return delegate;
    }

    @Override
    protected Object doClone(Object object) throws Throwable {
        return delegate.deepClone(object);
    }

}
