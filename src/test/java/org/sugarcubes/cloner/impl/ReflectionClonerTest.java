package org.sugarcubes.cloner.impl;

import org.sugarcubes.cloner.AbstractClonerTests;
import org.sugarcubes.cloner.Cloner;
import org.sugarcubes.cloner.impl.ReflectionCloner;

/**
 * @author Maxim Butov
 */
public class ReflectionClonerTest extends AbstractClonerTests {

    @Override
    protected Cloner getCloner() {
        return new ReflectionCloner();
    }

}
