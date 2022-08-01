package org.sugarcubes.cloner.thirdparty;

import org.sugarcubes.cloner.AbstractClonerTests;
import org.sugarcubes.cloner.Cloner;

public class KKClonerTest extends AbstractClonerTests {

    @Override
    protected Cloner getCloner() {
        return new KKCloner();
    }

}
