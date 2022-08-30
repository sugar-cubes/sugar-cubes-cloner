package org.sugarcubes.cloner;

class UnsafeReflectionClonerTest extends AbstractClonerTests {

    public UnsafeReflectionClonerTest() {
        super(Cloners.builder().setUnsafe().build());
    }

}
