package org.sugarcubes.cloner.thirdparty;

import org.sugarcubes.cloner.AbstractClonerTests;
import org.sugarcubes.cloner.Cloner;

/**
 * @author Maxim Butov
 */
public class FstClonerTest extends AbstractClonerTests {

    @Override
    protected Cloner getCloner() {
        return new FstCloner();
    }

}
