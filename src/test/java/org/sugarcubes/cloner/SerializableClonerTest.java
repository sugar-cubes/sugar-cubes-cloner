package org.sugarcubes.cloner;

/**
 * @author Maxim Butov
 */
public class SerializableClonerTest extends AbstractClonerTests {

    @Override
    protected Cloner getCloner() {
        return new SerializableCloner();
    }

}
