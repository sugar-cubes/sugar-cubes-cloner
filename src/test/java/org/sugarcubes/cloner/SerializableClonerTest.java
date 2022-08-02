package org.sugarcubes.cloner;

import org.sugarcubes.cloner.AbstractClonerTests;
import org.sugarcubes.cloner.Cloner;
import org.sugarcubes.cloner.SerializableCloner;

/**
 * @author Maxim Butov
 */
public class SerializableClonerTest extends AbstractClonerTests {

    @Override
    protected Cloner getCloner() {
        return new SerializableCloner();
    }

}
