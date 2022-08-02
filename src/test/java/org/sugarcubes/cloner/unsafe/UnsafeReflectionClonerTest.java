package org.sugarcubes.cloner.unsafe;

import org.sugarcubes.cloner.AbstractClonerTests;
import org.sugarcubes.cloner.Cloner;

/**
 * @author Maxim Butov
 */
class UnsafeReflectionClonerTest extends AbstractClonerTests {

    @Override
    protected Cloner getCloner() {
        return new UnsafeCloner();
    }

}
