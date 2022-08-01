package org.sugarcubes.cloner.other;

import org.sugarcubes.cloner.AbstractClonerTests;
import org.sugarcubes.cloner.Cloner;

/**
 * @author Maxim Butov
 */
public class KryoClonerTest extends AbstractClonerTests {

    @Override
    protected Cloner getCloner() {
        return new KryoCloner();
    }

}
