package org.sugarcubes.cloner;

import org.junit.jupiter.api.Test;

class UnsafeReflectionClonerTest extends AbstractClonerTests {

    public UnsafeReflectionClonerTest() {
        super(new ReflectionClonerBuilder().setUnsafe().build());
    }

    @Test
    @Override
    void testSimpleFields() {
        super.testSimpleFields();
    }
}
