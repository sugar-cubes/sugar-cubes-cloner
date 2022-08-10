package org.sugarcubes.cloner;

class ReflectionClonerTest extends AbstractClonerTests {

    public ReflectionClonerTest() {
//        super(new ReflectionCloner());
        super(new ReflectionCloner().parallel());
    }

}
