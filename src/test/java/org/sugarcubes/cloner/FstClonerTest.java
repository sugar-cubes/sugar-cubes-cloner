package org.sugarcubes.cloner;

import org.sugarcubes.cloner.other.FstCloner;
import org.sugarcubes.cloner.other.KryoCloner;

/**
 * @author Maxim Butov
 */
public class FstClonerTest extends AbstractClonerTests {

    @Override
    protected Cloner getCloner() {
        return new FstCloner();
    }

}
