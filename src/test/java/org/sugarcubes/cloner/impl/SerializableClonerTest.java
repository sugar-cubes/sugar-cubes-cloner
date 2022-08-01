package org.sugarcubes.cloner.impl;

import org.sugarcubes.cloner.AbstractClonerTests;
import org.sugarcubes.cloner.Cloner;
import org.sugarcubes.cloner.impl.SerializableCloner;

/**
 * @author Maxim Butov
 */
public class SerializableClonerTest extends AbstractClonerTests {

    @Override
    protected Cloner getCloner() {
        return new SerializableCloner();
    }

}
