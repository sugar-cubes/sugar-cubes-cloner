package org.sugarcubes.cloner;

/**
 * @author Maxim Butov
 */
public class ReflectionClonerTest extends AbstractClonerTests {

    @Override
    protected Cloner getCloner() {
        return new ReflectionCloner();
    }

}
