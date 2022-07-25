package org.sugarcubes.cloner;

import org.sugarcubes.cloner.other.KryoCloner;

/**
 * @author Maxim Butov
 */
public class KryoClonerTest extends AbstractClonerTests {

    @Override
    protected Cloner getCloner() {
        return new KryoCloner();
    }

}
