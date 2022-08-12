package org.sugarcubes.cloner;

import org.junit.jupiter.api.Test;

class UnsafeReflectionClonerTest extends AbstractClonerTests {

    public UnsafeReflectionClonerTest() {
        super(new ReflectionClonerBuilder().setUnsafeEnabled().build());
    }

    @Test
    @Override
    void testSimpleFields() {
        super.testSimpleFields();
    }
}
